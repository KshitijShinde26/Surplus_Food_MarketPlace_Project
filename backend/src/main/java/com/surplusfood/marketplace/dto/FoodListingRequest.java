package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.ListingType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FoodListingRequest(
        @NotBlank(message = "Food name is required")
        @Size(max = 140, message = "Food name must be 140 characters or fewer")
        String name,

        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        BigDecimal originalPrice,

        BigDecimal discountPrice,

        @NotNull(message = "Listing type is required")
        ListingType listingType,

        boolean vegetarian,

        boolean vegan,

        @NotNull(message = "Expiry time is required")
        @Future(message = "Expiry time must be in the future")
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiryTime,

        @NotNull(message = "Pickup start time is required")
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime pickupStartTime,

        @NotNull(message = "Pickup end time is required")
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime pickupEndTime,

        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
        BigDecimal longitude,

        List<FoodListingImageRequest> images
) {
}
