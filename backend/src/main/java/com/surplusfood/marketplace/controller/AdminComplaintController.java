package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.ComplaintResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.ComplaintStatus;
import com.surplusfood.marketplace.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public PageResponse<ComplaintResponse> searchComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return complaintService.searchComplaints(status, businessId, pageable);
    }

    @PatchMapping("/{id}/status")
    public ComplaintResponse updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam ComplaintStatus status
    ) {
        return complaintService.updateComplaintStatus(id, status);
    }
}
