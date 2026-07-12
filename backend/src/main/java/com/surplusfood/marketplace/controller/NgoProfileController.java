package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.NgoProfileRequest;
import com.surplusfood.marketplace.dto.NgoProfileResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.NgoService;
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
@RequestMapping("/ngo/profile")
@RequiredArgsConstructor
public class NgoProfileController {

    private final NgoService ngoService;

    @PostMapping
    @PreAuthorize("hasRole('NGO')")
    public ResponseEntity<NgoProfileResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NgoProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ngoService.createProfile(principal.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('NGO')")
    public NgoProfileResponse myProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ngoService.getMyProfile(principal.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('NGO')")
    public NgoProfileResponse updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NgoProfileRequest request
    ) {
        return ngoService.updateMyProfile(principal.getId(), request);
    }
}
