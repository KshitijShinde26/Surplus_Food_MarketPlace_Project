package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.FoodListingRequest;
import com.surplusfood.marketplace.dto.FoodListingResponse;
import com.surplusfood.marketplace.dto.MessageResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/food-listings")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService foodListingService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<FoodListingResponse> createListing(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FoodListingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodListingService.createListing(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public FoodListingResponse updateListing(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody FoodListingRequest request
    ) {
        return foodListingService.updateListing(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public MessageResponse deleteListing(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        foodListingService.deleteListing(principal.getId(), id);
        return new MessageResponse("Food listing deleted successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public PageResponse<FoodListingResponse> getMyListings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return foodListingService.getMyListings(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    public FoodListingResponse getListing(@PathVariable Long id) {
        return foodListingService.getListingById(id);
    }

    @GetMapping
    public PageResponse<FoodListingResponse> searchListings(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Boolean vegan,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return foodListingService.searchActiveListings(
                categoryId, type, vegetarian, vegan, keyword, latitude, longitude, radius, pageable);
    }
}
