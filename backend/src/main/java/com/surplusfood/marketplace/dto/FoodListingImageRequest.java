package com.surplusfood.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

public record FoodListingImageRequest(
        @NotBlank(message = "Image URL is required")
        String imageUrl,

        @NotBlank(message = "Cloudinary public ID is required")
        String cloudinaryPublicId,

        int sortOrder
) {
}
