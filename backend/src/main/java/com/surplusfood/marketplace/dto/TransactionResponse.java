package com.surplusfood.marketplace.dto;

import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long businessId,
        String businessName,
        Long orderId,
        Long donationId,
        TransactionType transactionType,
        BigDecimal amount,
        TransactionStatus status,
        Instant createdAt
) {
}
