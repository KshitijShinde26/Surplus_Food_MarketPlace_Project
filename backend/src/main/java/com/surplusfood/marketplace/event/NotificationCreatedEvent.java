package com.surplusfood.marketplace.event;

import com.surplusfood.marketplace.dto.NotificationResponse;

public record NotificationCreatedEvent(
        NotificationResponse response,
        String recipientEmail,
        String recipientFullName
) {
}
