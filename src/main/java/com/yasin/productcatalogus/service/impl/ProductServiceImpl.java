package com.yasin.productcatalogus.service.impl;

import com.yasin.productcatalogus.model.dto.PagedResponseDTO;
import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.entity.Product;
import com.yasin.productcatalogus.model.enums.Category;
import com.yasin.productcatalogus.model.mapper.ProductMapper;
import com.yasin.productcatalogus.repository.ProductRepository;
import com.yasin.productcatalogus.service.ProductService;
import com.yasin.productcatalogus.utilities.ResourceNotFoundExceptionUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public ProductResDTO createProduct(ProductReqDTO req) {
        Product saved = repository.save(mapper.toEntity(req));
        log.info("Created product with id={} name={}", saved.getProductId(), saved.getProductName());
        return mapper.toDto(saved);
    }

    @Override
    public ProductResDTO getProduct(Long id) {
        log.debug("Fetching product with id={}", id);
        return mapper.toDto(findOrThrow(id));
    }

    @Override
    public PagedResponseDTO<ProductResDTO> getProducts(Category category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Searching products with category={} minPrice={} maxPrice={} pageable={}", category, minPrice, maxPrice, pageable);
        Specification<Product> spec = Specification.unrestricted();

        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productCategory"), category));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Page<ProductResDTO> page = repository.findAll(spec, pageable).map(mapper::toDto);
        log.info("Found {} products (page {}/{})", page.getTotalElements(), page.getNumber(), page.getTotalPages());

        return new PagedResponseDTO<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public ProductResDTO updateProduct(Long id, ProductReqDTO req) {
        Product existing = findOrThrow(id);
        mapper.updateEntity(req, existing);

        ProductResDTO updated = mapper.toDto(repository.save(existing));
        log.info("Updated product with id={}", id);
        return updated;
    }

    @Override
    public void deleteProduct(Long id) {
        repository.delete(findOrThrow(id));
        log.info("Deleted product with id={}", id);
    }

    private Product findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id={}", id);
                    return new ResourceNotFoundExceptionUtility("Product not found");
                });
    }
}