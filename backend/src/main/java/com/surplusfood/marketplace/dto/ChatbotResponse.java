package com.surplusfood.marketplace.dto;

public record ChatbotResponse(
        String foodName,
        String foodCategory,
        String quantity,
        String preparationTime,
        String pickupDeadline,
        String storageType,
        String foodType,
        String packagingStatus,
        String specialInstructions,
        String confidence
) {}
