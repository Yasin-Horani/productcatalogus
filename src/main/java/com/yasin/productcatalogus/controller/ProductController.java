package com.yasin.productcatalogus.controller;

import com.yasin.productcatalogus.model.dto.PagedResponseDTO;
import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.enums.Category;
import com.yasin.productcatalogus.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping(value= "/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    // add new product
    @PostMapping(value= "/product")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResDTO create(@Valid @RequestBody ProductReqDTO req) {
        log.info("POST /products - new product: {}", req.getName());
        return service.createProduct(req);
    }

    // get product by id
    @GetMapping(value= "/product/{id}")
    public ProductResDTO get(@PathVariable Long id) {
        log.info("GET /products/{}", id);
        return service.getProduct(id);
    }

    // get all products with optional filters and pagination
    @GetMapping(value= "/product")
    public PagedResponseDTO<ProductResDTO> getAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price,asc") String[] sort) {

        log.info("GET /products - category={} minPrice={} maxPrice={} page={} size={}", category, minPrice, maxPrice, page, size);
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return service.getProducts(category, minPrice, maxPrice, pageable);
    }

    // update product by id
    @PutMapping(value= "/product/{id}")
    public ProductResDTO update(@PathVariable Long id, @Valid @RequestBody ProductReqDTO req) {
        log.info("PUT /products/{}", id);
        return service.updateProduct(id, req);
    }

    // delete product by id
    @DeleteMapping(value = "/product/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /products/{}", id);
        service.deleteProduct(id);
    }
}
