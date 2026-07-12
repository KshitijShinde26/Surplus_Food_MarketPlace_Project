package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.BusinessResponse;
import com.surplusfood.marketplace.entity.Business;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public BusinessResponse toResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getOwner().getId(),
                business.getOwner().getFullName(),
                business.getOwner().getEmail(),
                business.getOwner().getAccountStatus(),
                business.getBusinessName(),
                business.getBusinessType(),
                business.getLicenseNumber(),
                business.getAddressLine(),
                business.getCity(),
                business.getState(),
                business.getPostalCode(),
                business.getLatitude(),
                business.getLongitude(),
                business.isVerified(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}
