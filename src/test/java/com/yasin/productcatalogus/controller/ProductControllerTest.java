package com.yasin.productcatalogus.controller;

import com.yasin.productcatalogus.model.dto.PagedResponseDTO;
import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.enums.Category;
import com.yasin.productcatalogus.service.ProductService;
import com.yasin.productcatalogus.utilities.ResourceNotFoundExceptionUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String VALID_BODY = """
            {"name":"Clean Code","price":60.00,"category":"BOOKS","stock":5}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    private static ProductResDTO response() {
        ProductResDTO dto = new ProductResDTO();
        dto.setId(1L);
        dto.setName("Clean Code");
        dto.setPrice(new BigDecimal("60.00"));
        dto.setCategory(Category.BOOKS);
        dto.setStock(5);
        dto.setCreatedAt("09/09/2026 19:42");
        dto.setEffectivePrice(new BigDecimal("54.00"));
        return dto;
    }

    @Nested
    @DisplayName("POST /products")
    class Create {

        @Test
        @DisplayName("returns 201 with the created product")
        void createsProduct() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductReqDTO.class))).thenReturn(response());

            // Act & Assert
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Clean Code"));
        }

        @Test
        @DisplayName("passes the deserialized request body to the service")
        void forwardsRequestBody() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductReqDTO.class))).thenReturn(response());
            ArgumentCaptor<ProductReqDTO> captor = ArgumentCaptor.forClass(ProductReqDTO.class);

            // Act
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isCreated());

            // Assert
            verify(service).createProduct(captor.capture());
            ProductReqDTO sent = captor.getValue();
            assertThat(sent.getName()).isEqualTo("Clean Code");
            assertThat(sent.getPrice()).isEqualByComparingTo("60.00");
            assertThat(sent.getCategory()).isEqualTo(Category.BOOKS);
            assertThat(sent.getStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("renders prices with two decimals and the dd/MM/yyyy HH:mm date format")
        void rendersFormattedFields() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductReqDTO.class))).thenReturn(response());

            // Act & Assert
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("\"price\":60.00")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("\"effectivePrice\":54.00")))
                    .andExpect(jsonPath("$.createdAt").value("09/09/2026 19:42"));
        }

        @Test
        @DisplayName("returns 400 when the name is blank")
        void rejectsBlankName() throws Exception {
            // Arrange
            String body = """
                    {"name":"  ","price":60.00,"category":"BOOKS","stock":5}
                    """;

            // Act & Assert
            mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.messages[0]").value("name cannot be empty"));

            verify(service, never()).createProduct(any());
        }

        @Test
        @DisplayName("returns 400 when the price is negative")
        void rejectsNegativePrice() throws Exception {
            // Arrange
            String body = """
                    {"name":"Clean Code","price":-1.00,"category":"BOOKS","stock":5}
                    """;

            // Act & Assert
            mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages[0]").value("price must be >= 0"));
        }

        @Test
        @DisplayName("returns 400 when the price has more than two decimals")
        void rejectsTooManyDecimals() throws Exception {
            // Arrange
            String body = """
                    {"name":"Clean Code","price":10.555,"category":"BOOKS","stock":5}
                    """;

            // Act & Assert
            mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages[0]").value("price must have at most 2 decimals"));
        }

        @Test
        @DisplayName("returns 400 listing every missing required field")
        void rejectsMissingFields() throws Exception {
            // Arrange
            String body = "{}";

            // Act & Assert
            mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.messages", org.hamcrest.Matchers.hasSize(4)));
        }
    }

    @Nested
    @DisplayName("GET /products/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with the product")
        void returnsProduct() throws Exception {
            // Arrange
            when(service.getProduct(1L)).thenReturn(response());

            // Act & Assert
            mockMvc.perform(get("/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 404 when the product does not exist")
        void returnsNotFound() throws Exception {
            // Arrange
            when(service.getProduct(99L)).thenThrow(new ResourceNotFoundExceptionUtility("Product not found"));

            // Act & Assert
            mockMvc.perform(get("/products/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.messages[0]").value("Product not found"));
        }
    }

    @Nested
    @DisplayName("GET /products")
    class GetAll {

        private void stubEmptyPage() {
            when(service.getProducts(any(), any(), any(), any()))
                    .thenReturn(new PagedResponseDTO<>(List.of(), 0, 10, 0L, 0));
        }

        private Pageable capturePageable() {
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(service).getProducts(any(), any(), any(), captor.capture());
            return captor.getValue();
        }

        @Test
        @DisplayName("returns the paged payload")
        void returnsPagedPayload() throws Exception {
            // Arrange
            when(service.getProducts(any(), any(), any(), any()))
                    .thenReturn(new PagedResponseDTO<>(List.of(response()), 0, 10, 1L, 1));

            // Act & Assert
            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].id").value(1))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("uses page 0, size 10 and price ascending by default")
        void appliesDefaults() throws Exception {
            // Arrange
            stubEmptyPage();

            // Act
            mockMvc.perform(get("/products")).andExpect(status().isOk());

            // Assert
            verify(service).getProducts(eq(null), eq(null), eq(null), any(Pageable.class));
            Pageable pageable = capturePageable();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "price"));
        }

        @Test
        @DisplayName("forwards every filter to the service")
        void forwardsFilters() throws Exception {
            // Arrange
            stubEmptyPage();

            // Act
            mockMvc.perform(get("/products")
                            .param("category", "BOOKS")
                            .param("minPrice", "10.00")
                            .param("maxPrice", "99.99"))
                    .andExpect(status().isOk());

            // Assert
            verify(service).getProducts(
                    eq(Category.BOOKS),
                    eq(new BigDecimal("10.00")),
                    eq(new BigDecimal("99.99")),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("applies descending sorting when requested")
        void appliesDescendingSort() throws Exception {
            // Arrange
            stubEmptyPage();

            // Act
            mockMvc.perform(get("/products")
                            .param("page", "2")
                            .param("size", "5")
                            .param("sort", "name", "desc"))
                    .andExpect(status().isOk());

            // Assert
            Pageable pageable = capturePageable();
            assertThat(pageable.getPageNumber()).isEqualTo(2);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "name"));
        }

        @Test
        @DisplayName("falls back to ascending when only a sort field is given")
        void defaultsToAscendingWithoutDirection() throws Exception {
            // Arrange
            stubEmptyPage();

            // Act
            mockMvc.perform(get("/products").param("sort", "name"))
                    .andExpect(status().isOk());

            // Assert
            assertThat(capturePageable().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
        }

        @Test
        @DisplayName("returns 400 for an unknown category")
        void rejectsUnknownCategory() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/products").param("category", "TOYS"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
        }
    }

    @Nested
    @DisplayName("PUT /products/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with the updated product")
        void updatesProduct() throws Exception {
            // Arrange
            when(service.updateProduct(eq(1L), any(ProductReqDTO.class))).thenReturn(response());

            // Act & Assert
            mockMvc.perform(put("/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 404 when the product does not exist")
        void returnsNotFound() throws Exception {
            // Arrange
            when(service.updateProduct(eq(99L), any(ProductReqDTO.class)))
                    .thenThrow(new ResourceNotFoundExceptionUtility("Product not found"));

            // Act & Assert
            mockMvc.perform(put("/products/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("returns 400 for an invalid body without calling the service")
        void rejectsInvalidBody() throws Exception {
            // Arrange
            String body = """
                    {"name":"","price":60.00,"category":"BOOKS","stock":5}
                    """;

            // Act & Assert
            mockMvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(service, never()).updateProduct(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /products/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 with an empty body")
        void deletesProduct() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/products/1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).deleteProduct(1L);
        }

        @Test
        @DisplayName("returns 404 when the product does not exist")
        void returnsNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundExceptionUtility("Product not found"))
                    .when(service).deleteProduct(99L);

            // Act & Assert
            mockMvc.perform(delete("/products/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }
}
