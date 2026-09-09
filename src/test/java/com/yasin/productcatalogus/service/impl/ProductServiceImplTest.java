package com.yasin.productcatalogus.service.impl;

import com.yasin.productcatalogus.model.dto.PagedResponseDTO;
import com.yasin.productcatalogus.model.dto.ProductReqDTO;
import com.yasin.productcatalogus.model.dto.ProductResDTO;
import com.yasin.productcatalogus.model.entity.Product;
import com.yasin.productcatalogus.model.enums.Category;
import com.yasin.productcatalogus.model.mapper.ProductMapper;
import com.yasin.productcatalogus.repository.ProductRepository;
import com.yasin.productcatalogus.utilities.ResourceNotFoundExceptionUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl service;

    private static Product product(Long id, String name, BigDecimal price, Category category) {
        return Product.builder()
                .productId(id)
                .productName(name)
                .price(price)
                .productCategory(category)
                .productStock(5)
                .build();
    }

    private static ProductResDTO responseDto(Long id) {
        ProductResDTO dto = new ProductResDTO();
        dto.setId(id);
        return dto;
    }

    private static ProductReqDTO requestDto(String name, BigDecimal price, Category category, Integer stock) {
        ProductReqDTO dto = new ProductReqDTO();
        dto.setName(name);
        dto.setPrice(price);
        dto.setCategory(category);
        dto.setStock(stock);
        return dto;
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("maps the request, saves it and returns the mapped response")
        void savesMappedEntityAndReturnsDto() {
            // Arrange
            ProductReqDTO request = requestDto("Mouse", new BigDecimal("10.50"), Category.ELECTRONICS, 3);
            Product unsaved = product(null, "Mouse", new BigDecimal("10.50"), Category.ELECTRONICS);
            Product saved = product(1L, "Mouse", new BigDecimal("10.50"), Category.ELECTRONICS);
            ProductResDTO expected = responseDto(1L);

            when(mapper.toEntity(request)).thenReturn(unsaved);
            when(repository.save(unsaved)).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(expected);

            // Act
            ProductResDTO actual = service.createProduct(request);

            // Assert
            assertThat(actual).isSameAs(expected);
            verify(repository).save(unsaved);
        }
    }

    @Nested
    @DisplayName("getProduct")
    class GetProduct {

        @Test
        @DisplayName("returns the mapped product when it exists")
        void returnsDtoWhenFound() {
            // Arrange
            Product found = product(1L, "Mouse", new BigDecimal("10.50"), Category.ELECTRONICS);
            ProductResDTO expected = responseDto(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(found));
            when(mapper.toDto(found)).thenReturn(expected);

            // Act
            ProductResDTO actual = service.getProduct(1L);

            // Assert
            assertThat(actual).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the product does not exist")
        void throwsWhenMissing() {
            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.getProduct(99L))
                    .isInstanceOf(ResourceNotFoundExceptionUtility.class)
                    .hasMessage("Product not found");

            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("getProducts")
    class GetProducts {

        private final Pageable pageable = PageRequest.of(0, 2);

        @SuppressWarnings("unchecked")
        private Specification<Product> captureSpecification() {
            ArgumentCaptor<Specification<Product>> captor = ArgumentCaptor.forClass(Specification.class);
            verify(repository).findAll(captor.capture(), eq(pageable));
            return captor.getValue();
        }

        @SuppressWarnings("unchecked")
        private CriteriaBuilder applySpecification(Specification<Product> specification) {
            Root<Product> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder builder = mock(CriteriaBuilder.class);
            Path<Object> path = mock(Path.class);
            Predicate predicate = mock(Predicate.class);

            lenient().when(root.get(anyString())).thenReturn(path);
            lenient().when(builder.equal(any(), any())).thenReturn(predicate);
            lenient().when(builder.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
            lenient().when(builder.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
            lenient().when(builder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

            specification.toPredicate(root, query, builder);
            return builder;
        }

        @SuppressWarnings("unchecked")
        private void stubFindAllReturning(Product... products) {
            when(repository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(products), pageable, products.length));
        }

        @Test
        @DisplayName("maps every page entry and copies the paging metadata")
        void returnsMappedPage() {
            // Arrange
            Product first = product(1L, "Mouse", new BigDecimal("10.50"), Category.ELECTRONICS);
            Product second = product(2L, "Book", new BigDecimal("60.00"), Category.BOOKS);
            stubFindAllReturning(first, second);
            when(mapper.toDto(first)).thenReturn(responseDto(1L));
            when(mapper.toDto(second)).thenReturn(responseDto(2L));

            // Act
            PagedResponseDTO<ProductResDTO> actual = service.getProducts(null, null, null, pageable);

            // Assert
            assertThat(actual.getItems()).extracting(ProductResDTO::getId).containsExactly(1L, 2L);
            assertThat(actual.getPage()).isZero();
            assertThat(actual.getSize()).isEqualTo(2);
            assertThat(actual.getTotalItems()).isEqualTo(2);
            assertThat(actual.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns an empty page when nothing matches")
        void returnsEmptyPage() {
            // Arrange
            stubFindAllReturning();

            // Act
            PagedResponseDTO<ProductResDTO> actual = service.getProducts(null, null, null, pageable);

            // Assert
            assertThat(actual.getItems()).isEmpty();
            assertThat(actual.getTotalItems()).isZero();
            verify(mapper, never()).toDto(any());
        }

        @Test
        @DisplayName("adds no predicate when all filters are null")
        void buildsUnrestrictedSpecification() {
            // Arrange
            stubFindAllReturning();

            // Act
            service.getProducts(null, null, null, pageable);
            CriteriaBuilder builder = applySpecification(captureSpecification());

            // Assert
            verify(builder, never()).equal(any(), any());
            verify(builder, never()).greaterThanOrEqualTo(any(), any(BigDecimal.class));
            verify(builder, never()).lessThanOrEqualTo(any(), any(BigDecimal.class));
        }

        @Test
        @DisplayName("filters on category only")
        void buildsCategorySpecification() {
            // Arrange
            stubFindAllReturning();

            // Act
            service.getProducts(Category.BOOKS, null, null, pageable);
            CriteriaBuilder builder = applySpecification(captureSpecification());

            // Assert
            verify(builder).equal(any(), eq(Category.BOOKS));
            verify(builder, never()).greaterThanOrEqualTo(any(), any(BigDecimal.class));
            verify(builder, never()).lessThanOrEqualTo(any(), any(BigDecimal.class));
        }

        @Test
        @DisplayName("filters on minPrice only")
        void buildsMinPriceSpecification() {
            // Arrange
            stubFindAllReturning();

            // Act
            service.getProducts(null, new BigDecimal("10.00"), null, pageable);
            CriteriaBuilder builder = applySpecification(captureSpecification());

            // Assert
            verify(builder).greaterThanOrEqualTo(any(), eq(new BigDecimal("10.00")));
            verify(builder, never()).lessThanOrEqualTo(any(), any(BigDecimal.class));
            verify(builder, never()).equal(any(), any());
        }

        @Test
        @DisplayName("filters on maxPrice only")
        void buildsMaxPriceSpecification() {
            // Arrange
            stubFindAllReturning();

            // Act
            service.getProducts(null, null, new BigDecimal("99.99"), pageable);
            CriteriaBuilder builder = applySpecification(captureSpecification());

            // Assert
            verify(builder).lessThanOrEqualTo(any(), eq(new BigDecimal("99.99")));
            verify(builder, never()).greaterThanOrEqualTo(any(), any(BigDecimal.class));
            verify(builder, never()).equal(any(), any());
        }

        @Test
        @DisplayName("combines category, minPrice and maxPrice filters")
        void buildsCombinedSpecification() {
            // Arrange
            stubFindAllReturning();

            // Act
            service.getProducts(Category.BOOKS, new BigDecimal("10.00"), new BigDecimal("99.99"), pageable);
            CriteriaBuilder builder = applySpecification(captureSpecification());

            // Assert
            verify(builder).equal(any(), eq(Category.BOOKS));
            verify(builder).greaterThanOrEqualTo(any(), eq(new BigDecimal("10.00")));
            verify(builder).lessThanOrEqualTo(any(), eq(new BigDecimal("99.99")));
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("delegates the field updates to the mapper and saves the existing entity")
        void updatesAllFields() {
            // Arrange
            Product existing = product(1L, "Old", new BigDecimal("10.00"), Category.ELECTRONICS);
            ProductReqDTO request = requestDto("New", new BigDecimal("75.25"), Category.BOOKS, 42);
            ProductResDTO expected = responseDto(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDto(existing)).thenReturn(expected);

            // Act
            ProductResDTO actual = service.updateProduct(1L, request);

            // Assert
            assertThat(actual).isSameAs(expected);
            verify(mapper).updateEntity(request, existing);
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("throws and saves nothing when the product does not exist")
        void throwsWhenMissing() {
            // Arrange
            ProductReqDTO request = requestDto("New", new BigDecimal("75.25"), Category.BOOKS, 42);
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.updateProduct(99L, request))
                    .isInstanceOf(ResourceNotFoundExceptionUtility.class)
                    .hasMessage("Product not found");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("deletes the entity that was found")
        void deletesExistingProduct() {
            // Arrange
            Product existing = product(1L, "Mouse", new BigDecimal("10.50"), Category.ELECTRONICS);
            when(repository.findById(1L)).thenReturn(Optional.of(existing));

            // Act
            service.deleteProduct(1L);

            // Assert
            verify(repository).delete(existing);
        }

        @Test
        @DisplayName("throws and deletes nothing when the product does not exist")
        void throwsWhenMissing() {
            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteProduct(99L))
                    .isInstanceOf(ResourceNotFoundExceptionUtility.class)
                    .hasMessage("Product not found");

            verify(repository, never()).delete(any(Product.class));
        }
    }
}
