package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/businesses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBusinessController {

    private final BusinessService businessService;

    @GetMapping
    public PageResponse<BusinessResponse> searchBusinesses(
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return businessService.searchForAdmin(verified, keyword, page, size);
    }

    @PatchMapping("/{businessId}/verify")
    public BusinessResponse verifyBusiness(@PathVariable Long businessId) {
        return businessService.verifyBusiness(businessId);
    }

    @PatchMapping("/{businessId}/block")
    public BusinessResponse blockBusiness(@PathVariable Long businessId) {
        return businessService.blockBusiness(businessId);
    }

    @PatchMapping("/{businessId}/pending")
    public BusinessResponse markPending(@PathVariable Long businessId) {
        return businessService.markPending(businessId);
    }
}
