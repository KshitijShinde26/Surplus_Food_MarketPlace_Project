package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.PickupScheduleResponse;
import com.surplusfood.marketplace.entity.PickupStatus;
import com.surplusfood.marketplace.service.PickupScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pickups")
@RequiredArgsConstructor
public class PickupScheduleController {

    private final PickupScheduleService pickupScheduleService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public PickupScheduleResponse getScheduleByOrder(@PathVariable Long orderId) {
        return pickupScheduleService.getByOrderId(orderId);
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("isAuthenticated()")
    public PickupScheduleResponse getScheduleByDonation(@PathVariable Long donationId) {
        return pickupScheduleService.getByDonationId(donationId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public PickupScheduleResponse updatePickupStatus(
            @PathVariable Long id,
            @RequestParam PickupStatus status
    ) {
        return pickupScheduleService.updateStatus(id, status);
    }
}
