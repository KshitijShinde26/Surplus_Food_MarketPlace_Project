package com.surplusfood.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintRequest(
        Long businessId,
        Long listingId,

        @NotBlank(message = "Subject is required")
        @Size(max = 180, message = "Subject must be under 180 characters")
        String subject,

        @NotBlank(message = "Description is required")
        @Size(max = 1200, message = "Description must be under 1200 characters")
        String description
) {
}
