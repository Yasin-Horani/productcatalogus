package com.yasin.productcatalogus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponseDTO<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
