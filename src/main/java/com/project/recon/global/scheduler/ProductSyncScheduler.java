package com.project.recon.global.scheduler;

import com.project.recon.domain.product.service.ProductSearchService;
import com.project.recon.global.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncScheduler {

    private static final int CHUNK_SIZE = 1000;
    private final ProductSearchService productSearchService;
    private final StockService stockService;

    @Scheduled(cron = "0 0 4 * * *")
    public void sync() {
        log.info("[Scheduler] 전체 동기화 시작");

        productSearchService.syncIndexFromDB();
        stockService.syncStockFromDB();

        log.info("[Scheduler] 전체 동기화 완료");
    }
}
