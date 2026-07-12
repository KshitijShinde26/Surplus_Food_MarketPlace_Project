package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.DonationClaimRequest;
import com.surplusfood.marketplace.dto.DonationResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.Donation;
import com.surplusfood.marketplace.entity.DonationStatus;
import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.entity.ListingType;
import com.surplusfood.marketplace.entity.NgoProfile;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.DonationMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.DonationRepository;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.repository.NgoProfileRepository;
import com.surplusfood.marketplace.util.PageMapper;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
import com.surplusfood.marketplace.entity.User;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final FoodListingRepository foodListingRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final BusinessRepository businessRepository;
    private final DonationMapper donationMapper;
    private final NotificationService notificationService;
    private final PickupScheduleService pickupScheduleService;
    private final TransactionService transactionService;

    @Transactional
    public DonationResponse claimDonation(Long userId, DonationClaimRequest request) {
        NgoProfile ngo = ngoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        if (!ngo.isVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NGO organization profile must be verified by admin before claiming donations");
        }

        FoodListing listing = foodListingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found"));

        if (listing.getStatus() != FoodListingStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Food listing is no longer active");
        }

        if (listing.getListingType() != ListingType.FREE_DONATION) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only food listings marked as Free Donation can be claimed");
        }

        if (listing.getAvailableQuantity() < request.quantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Item already claimed or not enough stock available");
        }

        listing.setAvailableQuantity(listing.getAvailableQuantity() - request.quantity());
        if (listing.getAvailableQuantity() == 0) {
            listing.setStatus(FoodListingStatus.SOLD_OUT);
        }
        foodListingRepository.save(listing);

        Donation donation = new Donation();
        donation.setNgo(ngo);
        donation.setListing(listing);
        donation.setQuantity(request.quantity());
        donation.setStatus(DonationStatus.CLAIMED);
        donation.setConfirmationCode(generateConfirmationCode());

        Donation saved = donationRepository.save(donation);

        String msg = String.format("A new donation claim of %d x '%s' has been placed by NGO %s.",
                saved.getQuantity(), listing.getName(), ngo.getOrganizationName());
        notificationService.sendNotification(
                listing.getBusiness().getOwner(),
                "New Donation Claim Received",
                msg,
                NotificationType.DONATION_AVAILABLE
        );

        return donationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationResponse> getMyClaims(Long userId, Pageable pageable) {
        NgoProfile ngo = ngoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        Page<Donation> donations = donationRepository.findByNgoId(ngo.getId(), pageable);
        return PageMapper.toResponse(donations, donationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationResponse> getBusinessClaims(Long ownerId, Pageable pageable) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        Page<Donation> donations = donationRepository.findByListingBusinessId(business.getId(), pageable);
        return PageMapper.toResponse(donations, donationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DonationResponse getDonationById(Long userId, Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found"));

        boolean isNgo = donation.getNgo().getUser().getId().equals(userId);
        boolean isBusiness = donation.getListing().getBusiness().getOwner().getId().equals(userId);

        if (!isNgo && !isBusiness) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this donation");
        }

        return donationMapper.toResponse(donation);
    }

    @Transactional
    public DonationResponse approveDonation(Long ownerId, Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found"));

        if (!donation.getListing().getBusiness().getOwner().getId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own the food listing for this donation");
        }

        if (donation.getStatus() != DonationStatus.CLAIMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Donation status must be CLAIMED to approve");
        }

        donation.setStatus(DonationStatus.APPROVED);
        Donation saved = donationRepository.save(donation);

        String msg = String.format("Your claim for %d x '%s' has been approved by the business owner. Pickup is scheduled at: %s",
                saved.getQuantity(), saved.getListing().getName(), saved.getListing().getPickupStartTime());
        notificationService.sendNotification(
                donation.getNgo().getUser(),
                "Donation Claim Approved",
                msg,
                NotificationType.ORDER_ACCEPTED
        );

        pickupScheduleService.createScheduleForDonation(saved, saved.getListing().getPickupStartTime());

        transactionService.logTransaction(
                saved.getListing().getBusiness(),
                null,
                saved,
                TransactionType.DONATION,
                BigDecimal.ZERO,
                TransactionStatus.SUCCESS
        );

        return donationMapper.toResponse(saved);
    }

    @Transactional
    public DonationResponse cancelDonation(Long userId, Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found"));

        boolean isNgo = donation.getNgo().getUser().getId().equals(userId);
        boolean isBusiness = donation.getListing().getBusiness().getOwner().getId().equals(userId);

        if (!isNgo && !isBusiness) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to cancel this donation");
        }

        if (donation.getStatus() == DonationStatus.PICKED_UP || donation.getStatus() == DonationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Donation cannot be cancelled in its current state: " + donation.getStatus());
        }

        donation.setStatus(DonationStatus.CANCELLED);

        FoodListing listing = donation.getListing();
        listing.setAvailableQuantity(listing.getAvailableQuantity() + donation.getQuantity());
        if (listing.getStatus() == FoodListingStatus.SOLD_OUT && listing.getAvailableQuantity() > 0) {
            listing.setStatus(FoodListingStatus.ACTIVE);
        }
        foodListingRepository.save(listing);

        Donation saved = donationRepository.save(donation);

        User recipient = isNgo ? donation.getListing().getBusiness().getOwner() : donation.getNgo().getUser();
        String initiatorName = isNgo ? donation.getNgo().getOrganizationName() : "The business owner";
        String msg = String.format("Donation claim for %d x '%s' has been cancelled by %s.",
                donation.getQuantity(), donation.getListing().getName(), initiatorName);
        notificationService.sendNotification(
                recipient,
                "Donation Claim Cancelled",
                msg,
                NotificationType.DONATION_AVAILABLE
        );

        return donationMapper.toResponse(saved);
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
