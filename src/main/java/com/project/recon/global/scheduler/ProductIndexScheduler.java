package com.project.recon.global.scheduler;

import com.project.recon.domain.product.document.ProductDocument;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexScheduler {

    private static final int CHUNK_SIZE = 1000;
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @Scheduled(cron = "0 0 4 * * *")
    public void syncIndex() {

        int page = 0;
        int totalIndexed = 0;

        log.info("[Scheduler] Elasticsearch 전체 동기화 시작");

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
            } catch (Exception e) {
                log.error("[Scheduler] 동기화 중 에러 발생 (Page: {}): {}", page, e.getMessage());
            }

            // 마지막
            if (productPage.isLast()) {
                break;
            }

            page++;
        }

        log.info("[Scheduler] Elasticsearch 전체 동기화 완료 (총 {}건)", totalIndexed);

    }
}
