package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType type,
        Instant readAt,
        Instant createdAt
) {
}
