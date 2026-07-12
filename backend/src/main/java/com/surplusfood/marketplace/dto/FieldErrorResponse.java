package com.surplusfood.marketplace.dto;

public record FieldErrorResponse(
        String field,
        String message
) {
}
