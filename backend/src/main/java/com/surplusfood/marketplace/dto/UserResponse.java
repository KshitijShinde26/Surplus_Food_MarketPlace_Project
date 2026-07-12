package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.AccountStatus;
import java.math.BigDecimal;
import java.util.Set;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        AccountStatus accountStatus,
        boolean emailVerified,
        BigDecimal latitude,
        BigDecimal longitude,
        Set<String> roles
) {
}
