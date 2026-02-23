package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Product> searchProducts(String keyword, CategoryType category, ProductSortType sortBy, String sortDirection, Pageable pageable) {

        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.productName.containsIgnoreCase(keyword));
        }

        if (category != null) {
            builder.and(product.category.eq(category));
        }

        JPAQuery<Product> query = jpaQueryFactory.selectFrom(product);

        // likeCount 정렬일 때 LEFT JOIN + GROUP BY
        if (sortBy == ProductSortType.LIKE_COUNT) {
            QProductLike productLike = QProductLike.productLike;
            query.leftJoin(productLike).on(productLike.product.eq(product))
                    .groupBy(product.id);
        }

        List<Product> products = query
                .where(builder)
                .orderBy(getOrderSpecifiers(sortBy, sortDirection))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // 다음 페이지 존재 여부 확인
                .fetch();

        boolean hasNext = products.size() > pageable.getPageSize();

        if (hasNext) {
            products.remove(products.size() - 1); // 마지막 1개 제거
        }

        return new SliceImpl<>(products, pageable, hasNext);
    }

    @Override
    public Slice<Product> searchProductByIds(List<Long> productIds, CategoryType category, ProductSortType sortBy, String sortDirection, Pageable pageable) {

        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(product.id.in(productIds));

        if (category != null) {
            builder.and(product.category.eq(category));
        }

        JPAQuery<Product> query = jpaQueryFactory.selectFrom(product);

        // likeCount 정렬일 때 LEFT JOIN + GROUP BY
        if (sortBy == ProductSortType.LIKE_COUNT) {
            QProductLike productLike = QProductLike.productLike;
            query.leftJoin(productLike).on(productLike.product.eq(product))
                    .groupBy(product.id);
        }

        List<Product> products = query
                .where(builder)
                .orderBy(getOrderSpecifiers(sortBy, sortDirection))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)  // 다음 페이지 존재 여부 확인
                .fetch();

        boolean hasNext = products.size() > pageable.getPageSize();

        if (hasNext) {
            products.remove(products.size() - 1); // 마지막 1개 제거
        }

        return new SliceImpl<>(products, pageable, hasNext);
    }


    private OrderSpecifier<?> getOrderSpecifiers(ProductSortType sortBy, String sortDirection) {
        QProduct product = QProduct.product;
        QProductLike productLike = QProductLike.productLike;

        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

        return switch (sortBy) {
            case PRICE -> isAsc ? product.price.asc() : product.price.desc();
            case NAME -> isAsc ? product.productName.asc() : product.productName.desc();
            case LIKE_COUNT -> isAsc ? productLike.count().asc() : productLike.count().desc();
            default -> isAsc ? product.createdAt.asc() : product.createdAt.desc();
        };
    }
}
