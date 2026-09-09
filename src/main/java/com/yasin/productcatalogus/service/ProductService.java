package com.yasin.productcatalogus.service;

import com.yasin.productcatalogus.model.dto.PagedResponseDTO;
import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.enums.Category;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductResDTO createProduct(ProductReqDTO req);
    ProductResDTO getProduct(Long id);
    PagedResponseDTO<ProductResDTO> getProducts(Category category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    ProductResDTO updateProduct(Long id, ProductReqDTO req);
    void deleteProduct(Long id);
}