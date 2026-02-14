package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.QProduct;
import com.querydsl.core.BooleanBuilder;
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
    public Slice<Product> searchProducts(String keyword, CategoryType category, Pageable pageable) {

        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.productName.containsIgnoreCase(keyword));
        }

        if (category != null) {
            builder.and(product.category.eq(category));
        }

        List<Product> products = jpaQueryFactory
                .selectFrom(product)
                .where(builder)
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // 다음 페이지 존재 여부 확인
                .fetch();

        boolean hasNext = products.size() > pageable.getPageSize();

        if (hasNext) {
            products.remove(products.size() - 1); // 마지막 1개 제거
        }

        return new SliceImpl<>(products, pageable, hasNext);
    }
}
