package com.surplusfood.marketplace.event;

import com.surplusfood.marketplace.dto.FoodListingResponse;

public record FoodListingCreatedEvent(FoodListingResponse response) {
}
