package com.surplusfood.marketplace.entity;

public enum PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    REFUNDED
}
