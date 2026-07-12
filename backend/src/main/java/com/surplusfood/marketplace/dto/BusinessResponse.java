package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.AccountStatus;
import com.surplusfood.marketplace.entity.BusinessType;
import java.math.BigDecimal;
import java.time.Instant;

public record BusinessResponse(
        Long id,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        AccountStatus ownerStatus,
        String businessName,
        BusinessType businessType,
        String licenseNumber,
        String addressLine,
        String city,
        String state,
        String postalCode,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean verified,
        Instant createdAt,
        Instant updatedAt
) {
}
