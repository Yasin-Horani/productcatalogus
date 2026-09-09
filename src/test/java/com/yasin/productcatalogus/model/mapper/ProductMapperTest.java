package com.yasin.productcatalogus.model.mapper;

import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.entity.Product;
import com.yasin.productcatalogus.model.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapperImpl();

    private static Product product(BigDecimal price, Category category) {
        return Product.builder()
                .productId(1L)
                .productName("Clean Code")
                .price(price)
                .productCategory(category)
                .productStock(5)
                .createdAt("09/09/2026 19:42")
                .build();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("maps every request field onto the entity")
        void mapsAllFields() {
            // Arrange
            ProductReqDTO request = new ProductReqDTO();
            request.setName("Mouse");
            request.setPrice(new BigDecimal("10.50"));
            request.setCategory(Category.ELECTRONICS);
            request.setStock(3);

            // Act
            Product actual = mapper.toEntity(request);

            // Assert
            assertThat(actual.getProductName()).isEqualTo("Mouse");
            assertThat(actual.getPrice()).isEqualByComparingTo("10.50");
            assertThat(actual.getProductCategory()).isEqualTo(Category.ELECTRONICS);
            assertThat(actual.getProductStock()).isEqualTo(3);
            assertThat(actual.getProductId()).isNull();
        }

        @Test
        @DisplayName("returns null for a null request")
        void returnsNullForNullInput() {
            // Arrange & Act
            Product actual = mapper.toEntity(null);

            // Assert
            assertThat(actual).isNull();
        }

        @ParameterizedTest(name = "a submitted price of {0} is stored as {1}")
        @CsvSource({
                "10, 10.00",
                "20.21, 20.21",
                "15.2, 15.20",
                "0, 0.00",
                "1, 1.00"
        })
        @DisplayName("stores the price with exactly two decimals")
        void normalizesPriceScale(BigDecimal submitted, String expected) {
            // Arrange
            ProductReqDTO request = new ProductReqDTO();
            request.setName("Mouse");
            request.setPrice(submitted);
            request.setCategory(Category.ELECTRONICS);
            request.setStock(3);

            // Act
            Product actual = mapper.toEntity(request);

            // Assert
            assertThat(actual.getPrice()).hasToString(expected);
        }

        @Test
        @DisplayName("leaves the price null when none is supplied")
        void keepsNullPrice() {
            // Arrange
            ProductReqDTO request = new ProductReqDTO();
            request.setName("Mouse");
            request.setCategory(Category.ELECTRONICS);
            request.setStock(3);

            // Act
            Product actual = mapper.toEntity(request);

            // Assert
            assertThat(actual.getPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("updateEntity")
    class UpdateEntity {

        @Test
        @DisplayName("overwrites the editable fields and keeps id and createdAt")
        void updatesEditableFields() {
            // Arrange
            Product existing = product(new BigDecimal("10.00"), Category.ELECTRONICS);
            ProductReqDTO request = new ProductReqDTO();
            request.setName("New name");
            request.setPrice(new BigDecimal("75.25"));
            request.setCategory(Category.BOOKS);
            request.setStock(42);

            // Act
            mapper.updateEntity(request, existing);

            // Assert
            assertThat(existing.getProductName()).isEqualTo("New name");
            assertThat(existing.getPrice()).hasToString("75.25");
            assertThat(existing.getProductCategory()).isEqualTo(Category.BOOKS);
            assertThat(existing.getProductStock()).isEqualTo(42);
            assertThat(existing.getProductId()).isEqualTo(1L);
            assertThat(existing.getCreatedAt()).isEqualTo("09/09/2026 19:42");
        }

        @ParameterizedTest(name = "an updated price of {0} is stored as {1}")
        @CsvSource({
                "10, 10.00",
                "20.21, 20.21",
                "15.2, 15.20"
        })
        @DisplayName("normalizes the updated price to two decimals")
        void normalizesUpdatedPrice(BigDecimal submitted, String expected) {
            // Arrange
            Product existing = product(new BigDecimal("10.00"), Category.ELECTRONICS);
            ProductReqDTO request = new ProductReqDTO();
            request.setName("Mouse");
            request.setPrice(submitted);
            request.setCategory(Category.ELECTRONICS);
            request.setStock(3);

            // Act
            mapper.updateEntity(request, existing);

            // Assert
            assertThat(existing.getPrice()).hasToString(expected);
        }
    }

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("maps every entity field onto the response")
        void mapsAllFields() {
            // Arrange
            Product entity = product(new BigDecimal("10.50"), Category.ELECTRONICS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("Clean Code");
            assertThat(actual.getCategory()).isEqualTo(Category.ELECTRONICS);
            assertThat(actual.getStock()).isEqualTo(5);
            assertThat(actual.getCreatedAt()).isEqualTo("09/09/2026 19:42");
        }

        @Test
        @DisplayName("returns null for a null entity")
        void returnsNullForNullInput() {
            // Arrange & Act
            ProductResDTO actual = mapper.toDto(null);

            // Assert
            assertThat(actual).isNull();
        }

        @Test
        @DisplayName("leaves prices null when the entity has no price")
        void keepsPricesNullWhenPriceMissing() {
            // Arrange
            Product entity = product(null, Category.BOOKS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getPrice()).isNull();
            assertThat(actual.getEffectivePrice()).isNull();
        }
    }

    @Nested
    @DisplayName("effectivePrice")
    class EffectivePrice {

        @Test
        @DisplayName("applies a 10% discount on books priced above the threshold")
        void discountsExpensiveBooks() {
            // Arrange
            Product entity = product(new BigDecimal("60.00"), Category.BOOKS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getEffectivePrice()).isEqualByComparingTo("54.00");
        }

        @Test
        @DisplayName("applies the discount exactly at the 50 threshold")
        void discountsAtThreshold() {
            // Arrange
            Product entity = product(new BigDecimal("50.00"), Category.BOOKS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getEffectivePrice()).isEqualByComparingTo("45.00");
        }

        @Test
        @DisplayName("does not discount books priced just below the threshold")
        void keepsPriceBelowThreshold() {
            // Arrange
            Product entity = product(new BigDecimal("49.99"), Category.BOOKS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getEffectivePrice()).isEqualByComparingTo("49.99");
        }

        @ParameterizedTest(name = "{0} priced at {1} is never discounted")
        @CsvSource({
                "ELECTRONICS, 60.00",
                "ELECTRONICS, 10.50",
                "CLOTHING, 99.99",
                "CLOTHING, 10.00"
        })
        @DisplayName("never discounts non-book categories")
        void neverDiscountsOtherCategories(Category category, BigDecimal price) {
            // Arrange
            Product entity = product(price, category);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getEffectivePrice()).isEqualByComparingTo(price);
        }
    }

    @Nested
    @DisplayName("price scale")
    class PriceScale {

        @ParameterizedTest(name = "{0} is rendered as {1}")
        @CsvSource({
                "10, 10.00",
                "10.5, 10.50",
                "10.50, 10.50",
                "0, 0.00"
        })
        @DisplayName("always exposes prices with two decimals")
        void padsPriceToTwoDecimals(BigDecimal stored, String expected) {
            // Arrange
            Product entity = product(stored, Category.ELECTRONICS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getPrice()).hasToString(expected);
            assertThat(actual.getEffectivePrice()).hasToString(expected);
        }

        @Test
        @DisplayName("rounds a discount half up to two decimals")
        void roundsDiscountHalfUp() {
            // Arrange
            Product entity = product(new BigDecimal("55.55"), Category.BOOKS);

            // Act
            ProductResDTO actual = mapper.toDto(entity);

            // Assert
            assertThat(actual.getEffectivePrice()).hasToString("50.00");
            assertThat(actual.getPrice()).hasToString("55.55");
        }
    }
}
