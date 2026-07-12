package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.dto.ReviewRequest;
import com.surplusfood.marketplace.dto.ReviewResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CONSUMER')")
    public ReviewResponse createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.createReview(principal.getId(), request);
    }

    @GetMapping("/business/{businessId}")
    public PageResponse<ReviewResponse> getBusinessReviews(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewService.getBusinessReviews(businessId, pageable);
    }

    @GetMapping("/business/{businessId}/average")
    public Double getAverageRating(@PathVariable Long businessId) {
        return reviewService.getAverageRating(businessId);
    }
}
