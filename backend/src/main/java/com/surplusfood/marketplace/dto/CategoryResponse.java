package com.surplusfood.marketplace.dto;

public record CategoryResponse(
        Long id,
        String name,
        boolean active
) {
}
