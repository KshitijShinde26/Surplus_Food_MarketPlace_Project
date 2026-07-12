package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long consumerId,
        String consumerName,
        Long listingId,
        String listingName,
        Long businessId,
        String businessName,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String pickupCode,
        Instant createdAt,
        Instant updatedAt
) {
}
