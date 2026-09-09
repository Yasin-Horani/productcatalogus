package com.yasin.productcatalogus.model.dto;
import com.yasin.productcatalogus.model.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductReqDTO {
    @NotBlank(message = "cannot be empty")
    private String name;

    @NotNull(message = "is required")
    @DecimalMin(value = "0.00", message = "must be >= 0")
    @Digits(integer = 8, fraction = 2, message = "must have at most 2 decimals")
    @Schema(type = "number", format = "double")
    private BigDecimal price;

    @NotNull(message = "is required")
    private Category category;

    @NotNull(message = "is required")
    @Min(value = 0, message = "must be >= 0")
    private Integer stock;
}