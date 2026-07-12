package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.entity.ListingType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record FoodListingResponse(
        Long id,
        Long businessId,
        String businessName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        int quantity,
        int availableQuantity,
        BigDecimal originalPrice,
        BigDecimal discountPrice,
        ListingType listingType,
        boolean vegetarian,
        boolean vegan,
        LocalDateTime expiryTime,
        LocalDateTime pickupStartTime,
        LocalDateTime pickupEndTime,
        BigDecimal latitude,
        BigDecimal longitude,
        FoodListingStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        List<FoodListingImageResponse> images
) {
}
