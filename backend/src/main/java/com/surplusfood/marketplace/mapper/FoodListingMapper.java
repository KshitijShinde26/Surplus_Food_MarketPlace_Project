package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.FoodListingImageResponse;
import com.surplusfood.marketplace.dto.FoodListingResponse;
import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingImage;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FoodListingMapper {

    public FoodListingResponse toResponse(FoodListing listing) {
        List<FoodListingImageResponse> imageResponses = listing.getImages().stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());

        return new FoodListingResponse(
                listing.getId(),
                listing.getBusiness().getId(),
                listing.getBusiness().getBusinessName(),
                listing.getCategory().getId(),
                listing.getCategory().getName(),
                listing.getName(),
                listing.getDescription(),
                listing.getQuantity(),
                listing.getAvailableQuantity(),
                listing.getOriginalPrice(),
                listing.getDiscountPrice(),
                listing.getListingType(),
                listing.isVegetarian(),
                listing.isVegan(),
                listing.getExpiryTime(),
                listing.getPickupStartTime(),
                listing.getPickupEndTime(),
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getStatus(),
                listing.getVersion(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                imageResponses
        );
    }

    public FoodListingImageResponse toImageResponse(FoodListingImage image) {
        return new FoodListingImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getCloudinaryPublicId(),
                image.getSortOrder()
        );
    }
}
