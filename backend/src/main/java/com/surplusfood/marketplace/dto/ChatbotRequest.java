package com.surplusfood.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatbotRequest(
        @NotBlank(message = "Description cannot be blank")
        String description
) {}
