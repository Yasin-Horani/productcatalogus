package com.yasin.productcatalogus.model.mapper;

import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.entity.Product;
import com.yasin.productcatalogus.model.enums.Category;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "name", target = "productName")
    @Mapping(source = "category", target = "productCategory")
    @Mapping(source = "stock", target = "productStock")
    Product toEntity(ProductReqDTO dto);

    @Mapping(source = "name", target = "productName")
    @Mapping(source = "category", target = "productCategory")
    @Mapping(source = "stock", target = "productStock")
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ProductReqDTO dto, @MappingTarget Product entity);

    @Mapping(source = "productId", target = "id")
    @Mapping(source = "productName", target = "name")
    @Mapping(source = "productCategory", target = "category")
    @Mapping(source = "productStock", target = "stock")
    @Mapping(target = "effectivePrice", ignore = true)
    ProductResDTO toDto(Product entity);

    @AfterMapping
    default void normalizePrice(ProductReqDTO dto, @MappingTarget Product entity) {
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice().setScale(2, RoundingMode.HALF_UP));
        }
    }

    @AfterMapping
    default void calculateEffectivePrice(Product entity, @MappingTarget ProductResDTO dto) {
        BigDecimal price = entity.getPrice();
        if (price == null) {
            return;
        }

        BigDecimal effective = entity.getProductCategory() == Category.BOOKS
                && price.compareTo(new BigDecimal("50")) >= 0
                ? price.multiply(new BigDecimal("0.90"))
                : price;

        dto.setPrice(price.setScale(2, RoundingMode.HALF_UP));
        dto.setEffectivePrice(effective.setScale(2, RoundingMode.HALF_UP));
    }
}