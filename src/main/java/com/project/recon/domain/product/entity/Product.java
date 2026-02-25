package com.project.recon.domain.product.entity;

import com.project.recon.domain.user.entity.User;
import com.project.recon.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stock", nullable = false)
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "product", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();


    public static Product createProduct(String productName, Long price, CategoryType category, String description, int stock, User seller) {
        return Product.builder()
                .productName(productName)
                .price(price)
                .category(category)
                .description(description)
                .stock(stock)
                .seller(seller)
                .build();
    }

    public void update(String productName, Long price, CategoryType category, String description) {
        if (productName != null) this.productName = productName;
        if (price != null) this.price = price;
        if (category != null) this.category = category;
        if (description != null) this.description = description;
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }


    public void addImage(ProductImage image) {
        this.images.add(image);
        image.assignProduct(this);
        image.assignImageOrder(this.images.size() - 1);
    }

    public void removeImage(ProductImage image) {
        this.images.remove(image);
    }
}
