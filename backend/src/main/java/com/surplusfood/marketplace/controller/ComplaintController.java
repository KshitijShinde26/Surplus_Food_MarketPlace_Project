package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.ComplaintRequest;
import com.surplusfood.marketplace.dto.ComplaintResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ComplaintResponse fileComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ComplaintRequest request
    ) {
        return complaintService.fileComplaint(principal.getId(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<ComplaintResponse> getMyComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return complaintService.getMyComplaints(principal.getId(), pageable);
    }
}
