package com.surplusfood.marketplace.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record NgoProfileRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 160, message = "Organization name must be 160 characters or fewer")
        String organizationName,

        @Size(max = 100, message = "Registration number must be 100 characters or fewer")
        String registrationNumber,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must be 255 characters or fewer")
        String addressLine,

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
