package com.yasin.productcatalogus.repository;

import com.yasin.productcatalogus.model.entity.Product;
import com.yasin.productcatalogus.model.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Product product(BigDecimal price) {
        return Product.builder()
                .productName("Mouse")
                .price(price)
                .productCategory(Category.ELECTRONICS)
                .productStock(3)
                .build();
    }

    @Test
    @DisplayName("stores the price in a NUMERIC(10,2) column")
    void priceColumnKeepsTwoDecimals() {
        // Arrange & Act
        Map<String, Object> column = jdbcTemplate.queryForMap("""
                SELECT data_type, numeric_precision, numeric_scale
                FROM information_schema.columns
                WHERE table_name = 'PRODUCT' AND column_name = 'PRODUCT_PRICE'
                """);

        // Assert
        assertThat(column.get("DATA_TYPE").toString()).containsIgnoringCase("NUMERIC");
        assertThat(((Number) column.get("NUMERIC_PRECISION")).intValue()).isEqualTo(10);
        assertThat(((Number) column.get("NUMERIC_SCALE")).intValue()).isEqualTo(2);
    }

    @ParameterizedTest(name = "a price of {0} is read back as {1}")
    @CsvSource({
            "10.00, 10.00",
            "20.21, 20.21",
            "15.20, 15.20",
            "0.00, 0.00",
            "1.00, 1.00"
    })
    @DisplayName("returns money with two decimals after a database round trip")
    void keepsTwoDecimalsAfterRoundTrip(BigDecimal stored, String expected) {
        // Arrange
        Product saved = repository.saveAndFlush(product(stored));

        // Act
        Product reloaded = repository.findById(saved.getProductId()).orElseThrow();

        // Assert
        assertThat(reloaded.getPrice()).hasToString(expected);
    }

    @Test
    @DisplayName("stores createdAt as dd/MM/yyyy HH:mm text in a VARCHAR column")
    void storesCreatedAtAsFormattedText() {
        // Arrange
        Product saved = repository.saveAndFlush(product(new BigDecimal("10.00")));

        // Act
        String stored = jdbcTemplate.queryForObject(
                "SELECT created_at FROM product WHERE product_id = ?", String.class, saved.getProductId());
        Map<String, Object> column = jdbcTemplate.queryForMap("""
                SELECT data_type
                FROM information_schema.columns
                WHERE table_name = 'PRODUCT' AND column_name = 'CREATED_AT'
                """);

        // Assert
        assertThat(column.get("DATA_TYPE").toString()).containsIgnoringCase("CHAR");
        assertThat(stored).matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("fills createdAt automatically on insert")
    void setsCreatedAtOnInsert() {
        // Arrange & Act
        Product saved = repository.saveAndFlush(product(new BigDecimal("10.00")));

        // Assert
        assertThat(saved.getCreatedAt()).matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("keeps the original createdAt when the product is updated")
    void keepsCreatedAtOnUpdate() {
        // Arrange
        Product saved = repository.saveAndFlush(product(new BigDecimal("10.00")));
        String original = saved.getCreatedAt();

        // Act
        saved.setProductName("Renamed");
        repository.saveAndFlush(saved);

        // Assert
        String stored = jdbcTemplate.queryForObject(
                "SELECT created_at FROM product WHERE product_id = ?", String.class, saved.getProductId());
        assertThat(stored).isEqualTo(original);
    }
}
