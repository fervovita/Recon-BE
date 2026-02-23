package com.project.recon.domain.product.service;

import com.project.recon.domain.product.document.ProductDocument;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexInitializer {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            log.info("인덱싱할 상품이 없습니다.");
            return;
        }

        log.info("Elasticsearch 상품 인덱싱 시작: {}건", products.size());

        List<ProductDocument> documents = products.stream()
                .map(ProductDocument::from)
                .toList();

        productSearchRepository.saveAll(documents);
        
        log.info("Elasticsearch 상품 인덱싱 완료");
    }
}
