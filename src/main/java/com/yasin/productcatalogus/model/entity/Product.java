package com.yasin.productcatalogus.model.entity;

import com.yasin.productcatalogus.model.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    public static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    private Category productCategory;

    @Column(name = "product_stock")
    private int productStock;

    @Column(name = "created_at", length = 16, updatable = false)
    private String createdAt;

    @PrePersist
    void applyCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now().format(CREATED_AT_FORMAT);
        }
    }
}