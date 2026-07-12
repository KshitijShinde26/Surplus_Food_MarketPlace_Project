package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.AdminAnalyticsResponse;
import com.surplusfood.marketplace.dto.BusinessAnalyticsResponse;
import com.surplusfood.marketplace.dto.NgoAnalyticsResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/business")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessAnalyticsResponse getBusinessAnalytics(@AuthenticationPrincipal UserPrincipal principal) {
        return analyticsService.getBusinessAnalytics(principal.getId());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminAnalyticsResponse getAdminAnalytics() {
        return analyticsService.getAdminAnalytics();
    }

    @GetMapping("/ngo")
    @PreAuthorize("hasRole('NGO')")
    public NgoAnalyticsResponse getNgoAnalytics(@AuthenticationPrincipal UserPrincipal principal) {
        return analyticsService.getNgoAnalytics(principal.getId());
    }
}
