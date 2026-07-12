package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.DonationResponse;
import com.surplusfood.marketplace.entity.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    public DonationResponse toResponse(Donation donation) {
        return new DonationResponse(
                donation.getId(),
                donation.getNgo().getId(),
                donation.getNgo().getOrganizationName(),
                donation.getListing().getId(),
                donation.getListing().getName(),
                donation.getListing().getBusiness().getId(),
                donation.getListing().getBusiness().getBusinessName(),
                donation.getQuantity(),
                donation.getStatus(),
                donation.getConfirmationCode(),
                donation.getCreatedAt()
        );
    }
}
