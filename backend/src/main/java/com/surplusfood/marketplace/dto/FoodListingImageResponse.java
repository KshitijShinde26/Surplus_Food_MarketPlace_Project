package com.surplusfood.marketplace.dto;

public record FoodListingImageResponse(
        Long id,
        String imageUrl,
        String cloudinaryPublicId,
        int sortOrder
) {
}
