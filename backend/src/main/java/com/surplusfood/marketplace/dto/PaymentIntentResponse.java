package com.surplusfood.marketplace.dto;

import java.math.BigDecimal;

public record PaymentIntentResponse(
        String clientSecret,
        String stripePaymentIntentId,
        Long orderId,
        BigDecimal amount
) {
}
