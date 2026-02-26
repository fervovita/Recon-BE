package com.project.recon.domain.order.service;

import com.project.recon.domain.cart.entity.CartItem;
import com.project.recon.domain.cart.repository.CartItemRepository;
import com.project.recon.domain.order.dto.OrderRequestDTO;
import com.project.recon.domain.order.dto.OrderResponseDTO;
import com.project.recon.domain.order.entity.Order;
import com.project.recon.domain.order.entity.OrderItem;
import com.project.recon.domain.order.entity.OrderStatus;
import com.project.recon.domain.order.repository.OrderRepository;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public OrderResponseDTO.OrderDetailResponseDTO createOrderFromCart(Long userId, OrderRequestDTO.CartOrderRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        // 장바구니 아이템 중복 제거
        List<Long> distinctCartItemIds = request.getCartItemIds().stream()
                .distinct()
                .toList();

        // 선택한 장바구니 아이템 조회
        List<CartItem> cartItems = cartItemRepository.findAllById(distinctCartItemIds).stream()
                .filter(item -> item.getCart().getUser().getId().equals(userId))
                .toList();

        if (cartItems.size() != distinctCartItemIds.size()) {
            throw new GeneralException(GeneralErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 재고 검증 & 총 가격 계산
        long totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
            }

            totalPrice += product.getPrice() * cartItem.getQuantity();
        }

        // 주문 생성
        List<OrderItem> orderItems = cartItems.stream()
                .map(ci -> OrderItem.createOrderItem(ci.getProduct(), ci.getQuantity()))
                .toList();

        Order order = Order.createOrder(user, totalPrice, orderItems);
        orderRepository.save(order);

        // 선택한 아이템을 장바구니에서 삭제
        cartItemRepository.deleteAll(cartItems);

        return toOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO.OrderDetailResponseDTO createDirectOrder(Long userId, OrderRequestDTO.DirectOrderRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 본인 상품인지 확인
        if (product.getSeller().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.ORDER_SELLER_NOT_ALLOWED);
        }

        // 재고 검증
        if (product.getStock() < request.getQuantity()) {
            throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
        }


        // 주문 생성
        long totalPrice = product.getPrice() * request.getQuantity();

        OrderItem orderItem = OrderItem.createOrderItem(product, request.getQuantity());

        Order order = Order.createOrder(user, totalPrice, List.of(orderItem));
        orderRepository.save(order);

        return toOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO.OrderDetailResponseDTO payOrder(Long userId, Long orderId) {

        // 주문 조회
        Order order = orderRepository.findByOrderIdWithItems(orderId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.ORDER_NOT_FOUND));

        // 본인 주문 여부 확인
        if (!order.getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.ORDER_NOT_BUYER);
        }

        // 상태 확인
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new GeneralException(GeneralErrorCode.ORDER_ALREADY_COMPLETED);
        }

        // Redis 재고 차감
        List<OrderItem> decreasedItems = new ArrayList<>();

        try {
            for (OrderItem item : order.getOrderItems()) {
                stockService.decreaseStock(item.getProduct().getId(), item.getQuantity());
                decreasedItems.add(item);
            }
        } catch (GeneralException e) {
            // 이미 차감한 재고 롤백
            for (OrderItem item : decreasedItems) {
                stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
            throw e;
        }

        // DB 재고 동기화
        for (OrderItem item : order.getOrderItems()) {
            int updatedCount = productRepository.decreaseStock(item.getProduct().getId(), item.getQuantity());
            if (updatedCount == 0) {
                throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
            }
        }

        // 주문 확정
        order.confirm();

        return toOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {

        // 주문 조회
        Order order = orderRepository.findByOrderIdWithItems(orderId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.ORDER_NOT_FOUND));

        // 본인 주문 여부 확인
        if (!order.getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.ORDER_NOT_BUYER);
        }

        // 이미 취소된 주문인지 확인
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new GeneralException(GeneralErrorCode.ORDER_ALREADY_CANCELLED);
        }

        // 결제가 완료된 주문이면 재고 복구
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getOrderItems()) {
                stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
                productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        // 주문 취소
        order.cancel();
    }

    @Override
    public List<OrderResponseDTO.OrderDetailResponseDTO> getOrders(Long userId) {

        // 주문 내약 조회
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);

        return orders.stream()
                .map(this::toOrderDetailResponse)
                .toList();
    }

    @Override
    public OrderResponseDTO.OrderDetailResponseDTO getOrder(Long userId, Long orderId) {

        // 주문 조회
        Order order = orderRepository.findByOrderIdWithItems(orderId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.ORDER_NOT_FOUND));

        // 본인 주문 여부 확인
        if (!order.getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.ORDER_NOT_BUYER);
        }

        return toOrderDetailResponse(order);
    }

    private OrderResponseDTO.OrderDetailResponseDTO toOrderDetailResponse(Order order) {
        List<OrderResponseDTO.OrderItemDTO> items = order.getOrderItems().stream()
                .map(item -> OrderResponseDTO.OrderItemDTO.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getProductName())
                        .quantity(item.getQuantity())
                        .orderPrice(item.getOrderPrice())
                        .totalPrice(item.getOrderPrice() * item.getQuantity())
                        .build())
                .toList();

        return OrderResponseDTO.OrderDetailResponseDTO.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
