package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.PickupStatus;
import java.time.LocalDateTime;

public record PickupScheduleResponse(
        Long id,
        Long orderId,
        Long donationId,
        LocalDateTime pickupTime,
        PickupStatus status,
        String notes
) {
}
