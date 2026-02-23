package com.project.recon.domain.product.event;


import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final ProductSearchService productSearchService;
    private final ProductRepository productRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductEvent(ProductEvent event) {
        switch (event.type()) {
            case CREATED, UPDATED -> {
                // DB에서 최신 데이터를 조회 후 ES에 인덱싱
                productRepository.findById(event.productId())
                        .ifPresent(productSearchService::indexProduct);
            }
            case DELETED -> {
                productSearchService.deleteProduct(event.productId());
            }
        }
    }

}
