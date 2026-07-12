package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.NgoProfileRequest;
import com.surplusfood.marketplace.dto.NgoProfileResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.AccountStatus;
import com.surplusfood.marketplace.entity.NgoProfile;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.NgoProfileMapper;
import com.surplusfood.marketplace.repository.NgoProfileRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NgoService {

    private final NgoProfileRepository ngoProfileRepository;
    private final UserRepository userRepository;
    private final NgoProfileMapper ngoProfileMapper;

    @Transactional
    public NgoProfileResponse createProfile(Long userId, NgoProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (ngoProfileRepository.findByUserId(userId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "NGO profile already exists for this account");
        }

        NgoProfile ngo = new NgoProfile();
        ngo.setUser(user);
        ngo.setOrganizationName(request.organizationName());
        ngo.setRegistrationNumber(request.registrationNumber());
        ngo.setAddressLine(request.addressLine());
        ngo.setLatitude(request.latitude());
        ngo.setLongitude(request.longitude());
        ngo.setVerified(false);

        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        NgoProfile saved = ngoProfileRepository.save(ngo);
        return ngoProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NgoProfileResponse getMyProfile(Long userId) {
        NgoProfile ngo = ngoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));
        return ngoProfileMapper.toResponse(ngo);
    }

    @Transactional
    public NgoProfileResponse updateMyProfile(Long userId, NgoProfileRequest request) {
        NgoProfile ngo = ngoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        ngo.setOrganizationName(request.organizationName());
        ngo.setRegistrationNumber(request.registrationNumber());
        ngo.setAddressLine(request.addressLine());
        ngo.setLatitude(request.latitude());
        ngo.setLongitude(request.longitude());
        ngo.setVerified(false);

        User user = ngo.getUser();
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        NgoProfile saved = ngoProfileRepository.save(ngo);
        return ngoProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<NgoProfileResponse> searchNgos(Boolean verified, String keyword, Pageable pageable) {
        Page<NgoProfile> page = ngoProfileRepository.searchNgoProfiles(verified, keyword, pageable);
        return PageMapper.toResponse(page, ngoProfileMapper::toResponse);
    }

    @Transactional
    public NgoProfileResponse verifyNgo(Long id) {
        NgoProfile ngo = ngoProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found"));

        ngo.setVerified(true);
        User user = ngo.getUser();
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        NgoProfile saved = ngoProfileRepository.save(ngo);
        return ngoProfileMapper.toResponse(saved);
    }

    @Transactional
    public NgoProfileResponse blockNgo(Long id) {
        NgoProfile ngo = ngoProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found"));

        ngo.setVerified(false);
        User user = ngo.getUser();
        user.setAccountStatus(AccountStatus.BLOCKED);
        userRepository.save(user);

        NgoProfile saved = ngoProfileRepository.save(ngo);
        return ngoProfileMapper.toResponse(saved);
    }

    @Transactional
    public NgoProfileResponse markNgoPending(Long id) {
        NgoProfile ngo = ngoProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found"));

        ngo.setVerified(false);
        User user = ngo.getUser();
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        NgoProfile saved = ngoProfileRepository.save(ngo);
        return ngoProfileMapper.toResponse(saved);
    }
}
