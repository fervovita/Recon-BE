package com.project.recon.domain.review.entity;

import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.user.entity.User;
import com.project.recon.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();


    public static Review createReview(String content, int rating, Product product, User user) {
        return Review.builder()
                .content(content)
                .rating(rating)
                .product(product)
                .user(user)
                .build();
    }

    public void updateReview(String content, Integer rating) {
        if (content != null) this.content = content;
        if (rating != null) this.rating = rating;
    }

    public void addImage(ReviewImage image) {
        images.add(image);
        image.assignReview(this);
    }
}
