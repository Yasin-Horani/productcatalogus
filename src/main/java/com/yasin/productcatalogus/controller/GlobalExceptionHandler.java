package com.yasin.productcatalogus.controller;

import com.yasin.productcatalogus.model.dto.ErrorResponseDTO;
import com.yasin.productcatalogus.utilities.ResourceNotFoundExceptionUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", errors);
        return new ErrorResponseDTO("VALIDATION_ERROR", errors);
    }

    @ExceptionHandler(ResourceNotFoundExceptionUtility.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundException(ResourceNotFoundExceptionUtility ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ErrorResponseDTO("NOT_FOUND", List.of(ex.getMessage()));
    }


    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("No static resource: {}", ex.getResourcePath());
        return new ErrorResponseDTO("NOT_FOUND", List.of("Resource not found"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleAllUncaughtException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return new ErrorResponseDTO("INTERNAL_ERROR", List.of("An unexpected error occurred"));
    }
}