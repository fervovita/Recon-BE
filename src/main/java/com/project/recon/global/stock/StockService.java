package com.project.recon.global.stock;

import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private static final String STOCK_PREFIX = "STOCK:";
    private static final int CHUNK_SIZE = 1000;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    public void setStock(Long productId, int stock) {
        redisTemplate.opsForValue().set(STOCK_PREFIX + productId, String.valueOf(stock));
    }

    public int getStock(Long productId) {
        Object value = redisTemplate.opsForValue().get(STOCK_PREFIX + productId);
        if (value == null) {
            throw new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND);
        }

        return Integer.parseInt(value.toString());
    }

    public long decreaseStock(Long productId, int quantity) {
        String key = STOCK_PREFIX + productId;
        Long result = redisTemplate.opsForValue().decrement(key, quantity);

        if (result == null) {
            throw new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND);
        }

        // 재고가 부족하면 롤백
        if (result < 0) {
            redisTemplate.opsForValue().increment(key, quantity);
            throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
        }

        return result;
    }

    public long increaseStock(Long productId, int quantity) {
        Long result = redisTemplate.opsForValue().increment(STOCK_PREFIX + productId, quantity);

        if (result == null) {
            throw new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND);
        }

        return result;
    }

    public void deleteStock(Long productId) {
        redisTemplate.delete(STOCK_PREFIX + productId);
    }

    public void syncStockFromDB() {

        int page = 0;
        int totalSynced = 0;

        log.info("[StockService] DB → Redis 재고 동기화 시작");

        while (true) {
            Page<Product> productPage = productRepository.findAll(PageRequest.of(page, CHUNK_SIZE));

            // 데이터가 0건인 경우
            if (productPage.isEmpty()) {
                break;
            }

            // 재고 데이터 저장
            productPage.getContent().forEach(product -> setStock(product.getId(), product.getStock()));

            // 총 데이터 수 저장
            totalSynced += productPage.getContent().size();

            // 마지막
            if (productPage.isLast()) {
                break;
            }

            page++;
        }

        log.info("[StockService] DB → Redis 재고 동기화 완료 (총 {} 건)", totalSynced);
    }
}
