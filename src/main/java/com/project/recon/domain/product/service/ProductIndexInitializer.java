package com.project.recon.domain.product.service;

import com.project.recon.domain.product.document.ProductDocument;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexInitializer {

    private static final int CHUNK_SIZE = 1000;     // 한번에 메모리에 올릴 데이터 수
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        int page = 0;
        int totalIndexed = 0;

        log.info("Elasticsearch 상품 인덱싱 시작 (Chunk size: {})", CHUNK_SIZE);

        while (true) {
            Page<Product> productPage = productRepository.findAll(PageRequest.of(page, CHUNK_SIZE));

            // 데이터가 0건인 경우
            if (productPage.isEmpty()) {
                break;
            }

            // DB Entity -> ES Document 변환
            List<ProductDocument> documents = productPage.getContent().stream()
                    .map(ProductDocument::from)
                    .toList();

            // ES에 insert
            try {
                productSearchRepository.saveAll(documents);
                totalIndexed += documents.size();
                log.info("인덱스 진행률: {}건 완료...", totalIndexed);
            } catch (Exception e) {
                log.error("인덱싱 중 에러 발생 (Page: {}): {}", page, e.getMessage());
            }

            // 마지막
            if (productPage.isLast()) {
                break;
            }

            page++;
        }

        log.info("Elasticsearch 총 {}건 상품 인덱싱 완료", totalIndexed);
    }
}
