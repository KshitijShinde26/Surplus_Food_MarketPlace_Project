package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.AccountStatus;
import java.math.BigDecimal;

public record NgoProfileResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        AccountStatus userStatus,
        String organizationName,
        String registrationNumber,
        String addressLine,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean verified
) {
}
