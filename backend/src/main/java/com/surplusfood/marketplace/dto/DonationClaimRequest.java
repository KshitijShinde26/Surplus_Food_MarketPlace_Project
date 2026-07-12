package com.surplusfood.marketplace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DonationClaimRequest(
        @NotNull(message = "Listing ID is required")
        Long listingId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
