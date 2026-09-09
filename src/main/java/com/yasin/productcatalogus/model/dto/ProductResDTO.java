package com.yasin.productcatalogus.model.dto;

import com.yasin.productcatalogus.model.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResDTO {
    private Long id;
    private String name;

    @Schema(type = "number", format = "double")
    private BigDecimal price;

    private Category category;
    private Integer stock;

    @Schema(example = "dd/MM/yyyy HH:mm")
    private String createdAt;

    @Schema(type = "number", format = "double")
    private BigDecimal effectivePrice;
}