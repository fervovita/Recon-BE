package com.project.recon.domain.order.entity;

import com.project.recon.domain.user.entity.User;
import com.project.recon.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order createOrder(User user, Long totalPrice, List<OrderItem> orderItems) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .build();

        orderItems.forEach(order::addOrderItem);

        return order;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    public void confirm() {
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        status = OrderStatus.CANCELLED;
    }
}
