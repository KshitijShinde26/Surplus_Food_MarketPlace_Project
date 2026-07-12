package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.FoodListingImageRequest;
import com.surplusfood.marketplace.dto.FoodListingRequest;
import com.surplusfood.marketplace.dto.FoodListingResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.Category;
import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingImage;
import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.FoodListingMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.CategoryRepository;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.util.PageMapper;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.repository.WishlistRepository;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.entity.Wishlist;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final FoodListingMapper foodListingMapper;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public FoodListingResponse createListing(Long ownerId, FoodListingRequest request) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        if (!business.isVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Business profile must be verified by admin before listing food");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        FoodListing listing = new FoodListing();
        listing.setBusiness(business);
        listing.setCategory(category);
        listing.setName(request.name());
        listing.setDescription(request.description());
        listing.setQuantity(request.quantity());
        listing.setAvailableQuantity(request.quantity());
        listing.setOriginalPrice(request.originalPrice());
        listing.setDiscountPrice(request.discountPrice());
        listing.setListingType(request.listingType());
        listing.setVegetarian(request.vegetarian());
        listing.setVegan(request.vegan());
        listing.setExpiryTime(request.expiryTime());
        listing.setPickupStartTime(request.pickupStartTime());
        listing.setPickupEndTime(request.pickupEndTime());

        BigDecimal lat = request.latitude() != null ? request.latitude() : business.getLatitude();
        BigDecimal lon = request.longitude() != null ? request.longitude() : business.getLongitude();
        listing.setLatitude(lat);
        listing.setLongitude(lon);
        listing.setStatus(FoodListingStatus.ACTIVE);

        if (request.images() != null) {
            for (FoodListingImageRequest imgReq : request.images()) {
                FoodListingImage img = new FoodListingImage();
                img.setImageUrl(imgReq.imageUrl());
                img.setCloudinaryPublicId(imgReq.cloudinaryPublicId());
                img.setSortOrder(imgReq.sortOrder());
                listing.addImage(img);
            }
        }

        FoodListing savedListing = foodListingRepository.save(listing);
        FoodListingResponse response = foodListingMapper.toResponse(savedListing);

        try {
            messagingTemplate.convertAndSend("/topic/listings", response);
            log.info("Broadcasted food listing to WebSocket topic.");
        } catch (Exception e) {
            log.warn("Failed to broadcast food listing: {}", e.getMessage());
        }

        if (lat != null && lon != null) {
            try {
                List<User> nearbyUsers = userRepository.findNearbyConsumersAndNgos(lat.doubleValue(), lon.doubleValue(), 10.0);
                Set<Long> notifiedUserIds = nearbyUsers.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

                String nearbyMsg = String.format("A new surplus food listing '%s' is available near you from %s!",
                        savedListing.getName(), business.getBusinessName());
                for (User user : nearbyUsers) {
                    notificationService.sendNotification(user, "New Food Nearby", nearbyMsg, NotificationType.NEW_FOOD_NEARBY);
                }

                List<Wishlist> wishlisters = wishlistRepository.findByBusinessId(business.getId());
                String wishlistMsg = String.format("Your favorite business '%s' just posted a new listing: '%s'!",
                        business.getBusinessName(), savedListing.getName());
                for (Wishlist w : wishlisters) {
                    User user = w.getUser();
                    if (!notifiedUserIds.contains(user.getId())) {
                        notificationService.sendNotification(user, "New Food from Favorite Business", wishlistMsg, NotificationType.NEW_FOOD_NEARBY);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to find nearby users or send notifications: {}. Continuing operation.", e.getMessage());
            }
        }

        return response;
    }

    @Transactional
    public FoodListingResponse updateListing(Long ownerId, Long listingId, FoodListingRequest request) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found"));

        if (!listing.getBusiness().getOwner().getId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }

        if (!listing.getBusiness().isVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Business profile must be verified");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        listing.setCategory(category);
        listing.setName(request.name());
        listing.setDescription(request.description());

        int diff = request.quantity() - listing.getQuantity();
        int newAvailable = listing.getAvailableQuantity() + diff;
        if (newAvailable < 0) {
            newAvailable = 0;
        }
        listing.setQuantity(request.quantity());
        listing.setAvailableQuantity(newAvailable);

        if (newAvailable > 0 && listing.getStatus() == FoodListingStatus.SOLD_OUT) {
            listing.setStatus(FoodListingStatus.ACTIVE);
        } else if (newAvailable == 0) {
            listing.setStatus(FoodListingStatus.SOLD_OUT);
        }

        listing.setOriginalPrice(request.originalPrice());
        listing.setDiscountPrice(request.discountPrice());
        listing.setListingType(request.listingType());
        listing.setVegetarian(request.vegetarian());
        listing.setVegan(request.vegan());
        listing.setExpiryTime(request.expiryTime());
        listing.setPickupStartTime(request.pickupStartTime());
        listing.setPickupEndTime(request.pickupEndTime());

        BigDecimal lat = request.latitude() != null ? request.latitude() : listing.getBusiness().getLatitude();
        BigDecimal lon = request.longitude() != null ? request.longitude() : listing.getBusiness().getLongitude();
        listing.setLatitude(lat);
        listing.setLongitude(lon);

        listing.getImages().clear();
        if (request.images() != null) {
            for (FoodListingImageRequest imgReq : request.images()) {
                FoodListingImage img = new FoodListingImage();
                img.setImageUrl(imgReq.imageUrl());
                img.setCloudinaryPublicId(imgReq.cloudinaryPublicId());
                img.setSortOrder(imgReq.sortOrder());
                listing.addImage(img);
            }
        }

        FoodListing savedListing = foodListingRepository.save(listing);
        return foodListingMapper.toResponse(savedListing);
    }

    @Transactional
    public void deleteListing(Long ownerId, Long listingId) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found"));

        if (!listing.getBusiness().getOwner().getId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }

        listing.setStatus(FoodListingStatus.REMOVED);
        foodListingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public FoodListingResponse getListingById(Long listingId) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found"));

        if (listing.getStatus() == FoodListingStatus.REMOVED) {
            throw new ResourceNotFoundException("Food listing has been removed");
        }

        return foodListingMapper.toResponse(listing);
    }

    @Transactional(readOnly = true)
    public PageResponse<FoodListingResponse> getMyListings(Long ownerId, Pageable pageable) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        Page<FoodListing> listings = foodListingRepository.findByBusinessIdAndStatusNot(
                business.getId(), FoodListingStatus.REMOVED, pageable);

        return PageMapper.toResponse(listings, foodListingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<FoodListingResponse> searchActiveListings(
            Long categoryId,
            String type,
            Boolean vegetarian,
            Boolean vegan,
            String keyword,
            Double latitude,
            Double longitude,
            Double radius,
            Pageable pageable
    ) {
        Page<FoodListing> page;
        LocalDateTime now = LocalDateTime.now();

        if (latitude != null && longitude != null) {
            double searchRadius = radius != null ? radius : 10.0; // default 10km
            page = foodListingRepository.findNearbyActiveListings(
                    latitude, longitude, searchRadius, categoryId, type, vegetarian, vegan, keyword, now, pageable);
        } else {
            page = foodListingRepository.findAllActiveListings(
                    categoryId, type, vegetarian, vegan, keyword, now, pageable);
        }

        return PageMapper.toResponse(page, foodListingMapper::toResponse);
    }
}
