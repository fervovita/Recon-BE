package com.project.recon.global.stock;

import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockService {

    private static final String STOCK_PREFIX = "STOCK:";
    private static final int CHUNK_SIZE = 1000;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final CircuitBreaker circuitBreaker;

    public StockService(
            RedisTemplate<String, Object> redisTemplate,
            ProductRepository productRepository,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("redis");
    }

    public void setStock(Long productId, int stock) {
        try {
            circuitBreaker.executeRunnable(() -> redisTemplate.opsForValue().set(STOCK_PREFIX + productId, String.valueOf(stock)));
        } catch (Exception e) {
            log.warn("[StockService] Redis 재고 설정 건너뜀: productId={}", productId, e);
        }

    }

    public boolean decreaseStock(Long productId, int quantity) {
        try {
            circuitBreaker.executeRunnable(() -> {
                String key = STOCK_PREFIX + productId;
                Long result = redisTemplate.opsForValue().decrement(key, quantity);

                if (result != null && result < 0) {
                    redisTemplate.opsForValue().increment(key, quantity);
                    throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
                }
            });

            return true;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[StockService] Redis 재고 차감 건너뜀: productId={}", productId, e);
            return false;
        }
    }

    public void increaseStock(Long productId, int quantity) {
        try {
            circuitBreaker.executeRunnable(() -> redisTemplate.opsForValue().increment(STOCK_PREFIX + productId, quantity));
        } catch (Exception e) {
            log.warn("[StockService] Redis 재고 증가 건너뜀: productId={}", productId, e);
        }
    }

    public void deleteStock(Long productId) {
        try {
            circuitBreaker.executeRunnable(() -> redisTemplate.delete(STOCK_PREFIX + productId));
        } catch (Exception e) {
            log.warn("[StockService] Redis 재고 삭제 건너뜀: productId={}", productId, e);
        }
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
