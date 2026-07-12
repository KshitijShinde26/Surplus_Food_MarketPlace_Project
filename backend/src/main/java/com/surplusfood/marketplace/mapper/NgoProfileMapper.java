package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.NgoProfileResponse;
import com.surplusfood.marketplace.entity.NgoProfile;
import org.springframework.stereotype.Component;

@Component
public class NgoProfileMapper {

    public NgoProfileResponse toResponse(NgoProfile ngo) {
        return new NgoProfileResponse(
                ngo.getId(),
                ngo.getUser().getId(),
                ngo.getUser().getFullName(),
                ngo.getUser().getEmail(),
                ngo.getUser().getAccountStatus(),
                ngo.getOrganizationName(),
                ngo.getRegistrationNumber(),
                ngo.getAddressLine(),
                ngo.getLatitude(),
                ngo.getLongitude(),
                ngo.isVerified()
        );
    }
}
