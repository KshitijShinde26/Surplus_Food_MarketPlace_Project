package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.dto.MessageResponse;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{businessId}")
    @PreAuthorize("hasRole('CONSUMER')")
    public MessageResponse addToWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long businessId
    ) {
        wishlistService.addToWishlist(principal.getId(), businessId);
        return new MessageResponse("Business added to wishlist successfully");
    }

    @DeleteMapping("/{businessId}")
    @PreAuthorize("hasRole('CONSUMER')")
    public MessageResponse removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long businessId
    ) {
        wishlistService.removeFromWishlist(principal.getId(), businessId);
        return new MessageResponse("Business removed from wishlist successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CONSUMER')")
    public List<BusinessResponse> getMyWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        return wishlistService.getMyWishlist(principal.getId());
    }

    @GetMapping("/{businessId}/status")
    @PreAuthorize("hasRole('CONSUMER')")
    public boolean checkWishlistStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long businessId
    ) {
        return wishlistService.isWishlisted(principal.getId(), businessId);
    }
}
