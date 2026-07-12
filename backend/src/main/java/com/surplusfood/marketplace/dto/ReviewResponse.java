package com.surplusfood.marketplace.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long consumerId,
        String consumerName,
        Long businessId,
        String businessName,
        Long orderId,
        int rating,
        String comment,
        Instant createdAt
) {
}
