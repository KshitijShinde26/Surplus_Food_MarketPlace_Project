package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.DonationStatus;
import java.time.Instant;

public record DonationResponse(
        Long id,
        Long ngoId,
        String organizationName,
        Long listingId,
        String listingName,
        Long businessId,
        String businessName,
        int quantity,
        DonationStatus status,
        String confirmationCode,
        Instant createdAt
) {
}
