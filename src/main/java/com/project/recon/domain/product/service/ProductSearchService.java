package com.project.recon.domain.product.service;

import com.project.recon.domain.product.entity.Product;

import java.util.List;

public interface ProductSearchService {

    List<Long> searchProductIds(String keyword);

    List<String> autoComplete(String keyword, int size);

    void indexProduct(Product product);

    void deleteProduct(Long productId);

    void syncIndexFromDB();
}
