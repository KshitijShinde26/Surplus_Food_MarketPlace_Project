package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.DonationClaimRequest;
import com.surplusfood.marketplace.dto.DonationResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping("/claim")
    @PreAuthorize("hasRole('NGO')")
    public ResponseEntity<DonationResponse> claimDonation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DonationClaimRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donationService.claimDonation(principal.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('NGO')")
    public PageResponse<DonationResponse> getMyClaims(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return donationService.getMyClaims(principal.getId(), pageable);
    }

    @GetMapping("/business")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public PageResponse<DonationResponse> getBusinessClaims(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return donationService.getBusinessClaims(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    public DonationResponse getDonationDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return donationService.getDonationById(principal.getId(), id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public DonationResponse approveDonation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return donationService.approveDonation(principal.getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public DonationResponse cancelDonation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return donationService.cancelDonation(principal.getId(), id);
    }
}
