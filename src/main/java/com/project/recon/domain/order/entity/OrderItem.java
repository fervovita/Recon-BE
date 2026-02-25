package com.project.recon.domain.order.entity;

import com.project.recon.domain.product.entity.Product;
import com.project.recon.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;


    public static OrderItem createOrderItem(Product product, int quantity) {
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .orderPrice(product.getPrice())
                .build();
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}
