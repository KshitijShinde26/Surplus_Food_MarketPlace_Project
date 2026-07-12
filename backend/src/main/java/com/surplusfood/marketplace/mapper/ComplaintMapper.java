package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.ComplaintResponse;
import com.surplusfood.marketplace.entity.Complaint;
import org.springframework.stereotype.Component;

@Component
public class ComplaintMapper {

    public ComplaintResponse toResponse(Complaint c) {
        if (c == null) {
            return null;
        }
        return new ComplaintResponse(
                c.getId(),
                c.getReporter().getId(),
                c.getReporter().getFullName(),
                c.getBusiness() != null ? c.getBusiness().getId() : null,
                c.getBusiness() != null ? c.getBusiness().getBusinessName() : null,
                c.getListing() != null ? c.getListing().getId() : null,
                c.getListing() != null ? c.getListing().getName() : null,
                c.getSubject(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt()
        );
    }
}
