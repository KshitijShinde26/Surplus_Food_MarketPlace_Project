package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.dto.TransactionResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.TransactionService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public PageResponse<TransactionResponse> getBusinessTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionService.getBusinessTransactions(principal.getId(), pageable);
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BigDecimal getTotalSalesEarnings(@AuthenticationPrincipal UserPrincipal principal) {
        return transactionService.getTotalSalesEarnings(principal.getId());
    }
}
