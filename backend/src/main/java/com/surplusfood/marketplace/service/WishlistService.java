package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.entity.Wishlist;
import com.surplusfood.marketplace.entity.WishlistId;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.BusinessMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Transactional
    public void addToWishlist(Long userId, Long businessId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        WishlistId id = new WishlistId(userId, businessId);
        if (wishlistRepository.existsById(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Business is already in your wishlist");
        }

        Wishlist wishlist = new Wishlist(user, business);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long businessId) {
        WishlistId id = new WishlistId(userId, businessId);
        if (!wishlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Wishlist entry not found");
        }
        wishlistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BusinessResponse> getMyWishlist(Long userId) {
        List<Wishlist> wishlist = wishlistRepository.findByUserId(userId);
        return wishlist.stream()
                .map(w -> businessMapper.toResponse(w.getBusiness()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(Long userId, Long businessId) {
        return wishlistRepository.existsByIdUserIdAndIdBusinessId(userId, businessId);
    }
}
