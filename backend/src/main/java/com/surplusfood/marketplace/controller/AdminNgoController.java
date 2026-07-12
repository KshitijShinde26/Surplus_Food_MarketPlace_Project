package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.NgoProfileResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.service.NgoService;
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
@RequestMapping("/admin/ngos")
@RequiredArgsConstructor
public class AdminNgoController {

    private final NgoService ngoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<NgoProfileResponse> listNgos(
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ngoService.searchNgos(verified, keyword, pageable);
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public NgoProfileResponse verifyNgo(@PathVariable Long id) {
        return ngoService.verifyNgo(id);
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public NgoProfileResponse blockNgo(@PathVariable Long id) {
        return ngoService.blockNgo(id);
    }

    @PatchMapping("/{id}/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public NgoProfileResponse markNgoPending(@PathVariable Long id) {
        return ngoService.markNgoPending(id);
    }
}
