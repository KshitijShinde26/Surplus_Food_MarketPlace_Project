package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.OrderRequest;
import com.surplusfood.marketplace.dto.OrderResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.OrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(principal.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CONSUMER')")
    public PageResponse<OrderResponse> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.getMyOrders(principal.getId(), pageable);
    }

    @GetMapping("/business")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public PageResponse<OrderResponse> getBusinessOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.getBusinessOrders(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return orderService.getOrderById(principal.getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return orderService.cancelOrder(principal.getId(), id);
    }
}
