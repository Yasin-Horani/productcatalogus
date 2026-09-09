package com.yasin.productcatalogus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private String error;
    private List<String> messages;
}