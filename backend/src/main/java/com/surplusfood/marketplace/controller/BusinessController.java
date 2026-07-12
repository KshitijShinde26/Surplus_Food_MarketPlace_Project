package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.BusinessProfileRequest;
import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/profile")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<BusinessResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(businessService.createProfile(principal.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessResponse myProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return businessService.getMyProfile(principal.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessResponse updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request
    ) {
        return businessService.updateMyProfile(principal.getId(), request);
    }
}
