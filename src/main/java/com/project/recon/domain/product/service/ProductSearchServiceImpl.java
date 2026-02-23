package com.project.recon.domain.product.service;

import com.project.recon.domain.product.document.ProductDocument;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductSearchRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final int MAX_SEARCH_RESULTS = 100;
    private final ElasticsearchOperations elasticsearchOperations;

    private final ProductSearchRepository productSearchRepository;

    // 상품 검색 : keyword로 ES에서 매칭되는 상품 ID만 반환
    @Override
    public List<Long> searchProductIds(String keyword) {
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(m -> m
                                    .query(keyword)
                                    .fields("productName^3", "description") // productName에 3배 가중치
                            )
                    )
                    .withMaxResults(MAX_SEARCH_RESULTS)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(ProductDocument::getId)
                    .toList();

        } catch (Exception e) {
            log.error("ElasticSearch 검색 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.SEARCH_FAILED);
        }
    }

    // 자동완성
    @Override
    public List<String> autoComplete(String keyword, int size) {
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .query(keyword)
                                    .field("productName.autocomplete")
                            )
                    )
                    .withMaxResults(size)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(ProductDocument::getProductName)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            log.error("ElasticSearch 자동완성 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.SEARCH_FAILED);
        }
    }

    // 인덱싱 : DB의 상품을 ES에 저장
    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void indexProduct(Product product) {
        productSearchRepository.save(ProductDocument.from(product));
    }

    @Recover
    public void recoverIndex(Exception e, Product product) {
        log.error("Elasticsearch 인덱싱 최종 실패 (productId={}): {}", product.getId(), e.getMessage());
    }

    // 삭제 : ES에서 상품 삭제
    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void deleteProduct(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    @Recover
    public void recoverDelete(Exception e, Long productId) {
        log.error("Elasticsearch 삭제 실패 (productId={}): {}", productId, e.getMessage());
    }
}
