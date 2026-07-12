package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.BusinessProfileRequest;
import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.AccountStatus;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ConflictException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.BusinessMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.util.PageMapper;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private static final int MAX_PAGE_SIZE = 50;

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessMapper businessMapper;

    @Transactional
    public BusinessResponse createProfile(Long ownerId, BusinessProfileRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business owner was not found"));

        if (businessRepository.existsByOwnerId(ownerId)) {
            throw new ConflictException("Business profile already exists for this account");
        }

        Business business = new Business();
        business.setOwner(owner);
        applyProfileRequest(business, request);
        business.setVerified(false);

        return businessMapper.toResponse(businessRepository.save(business));
    }

    @Transactional(readOnly = true)
    public BusinessResponse getMyProfile(Long ownerId) {
        return businessRepository.findByOwnerId(ownerId)
                .map(businessMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile was not found"));
    }

    @Transactional
    public BusinessResponse updateMyProfile(Long ownerId, BusinessProfileRequest request) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile was not found"));

        applyProfileRequest(business, request);
        business.setVerified(false);
        business.getOwner().setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        return businessMapper.toResponse(business);
    }

    @Transactional(readOnly = true)
    public PageResponse<BusinessResponse> searchForAdmin(Boolean verified, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim().toLowerCase(Locale.ROOT) : null;

        return PageMapper.toResponse(
                businessRepository.searchForAdmin(verified, normalizedKeyword, pageable),
                businessMapper::toResponse
        );
    }

    @Transactional
    public BusinessResponse verifyBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);
        business.setVerified(true);
        business.getOwner().setAccountStatus(AccountStatus.ACTIVE);
        return businessMapper.toResponse(business);
    }

    @Transactional
    public BusinessResponse blockBusiness(Long businessId) {
        Business business = getBusinessOrThrow(businessId);
        business.setVerified(false);
        business.getOwner().setAccountStatus(AccountStatus.BLOCKED);
        return businessMapper.toResponse(business);
    }

    @Transactional
    public BusinessResponse markPending(Long businessId) {
        Business business = getBusinessOrThrow(businessId);
        business.setVerified(false);
        business.getOwner().setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        return businessMapper.toResponse(business);
    }

    private Business getBusinessOrThrow(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile was not found"));
    }

    private void applyProfileRequest(Business business, BusinessProfileRequest request) {
        if (request.businessType() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Business type is required");
        }

        business.setBusinessName(request.businessName().trim());
        business.setBusinessType(request.businessType());
        business.setLicenseNumber(StringUtils.hasText(request.licenseNumber()) ? request.licenseNumber().trim() : null);
        business.setAddressLine(request.addressLine().trim());
        business.setCity(request.city().trim());
        business.setState(request.state().trim());
        business.setPostalCode(request.postalCode().trim());
        business.setLatitude(request.latitude());
        business.setLongitude(request.longitude());
    }
}
