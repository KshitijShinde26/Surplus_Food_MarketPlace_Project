package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.ComplaintStatus;
import java.time.Instant;

public record ComplaintResponse(
        Long id,
        Long reporterId,
        String reporterName,
        Long businessId,
        String businessName,
        Long listingId,
        String listingName,
        String subject,
        String description,
        ComplaintStatus status,
        Instant createdAt
) {
}
