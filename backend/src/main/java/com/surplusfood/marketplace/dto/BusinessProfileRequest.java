package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.BusinessType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BusinessProfileRequest(
        @NotBlank(message = "Business name is required")
        @Size(max = 160, message = "Business name must be 160 characters or fewer")
        String businessName,

        @NotNull(message = "Business type is required")
        BusinessType businessType,

        @Size(max = 100, message = "License number must be 100 characters or fewer")
        String licenseNumber,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must be 255 characters or fewer")
        String addressLine,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be 100 characters or fewer")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must be 100 characters or fewer")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(max = 30, message = "Postal code must be 30 characters or fewer")
        String postalCode,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
        BigDecimal longitude
) {
}
