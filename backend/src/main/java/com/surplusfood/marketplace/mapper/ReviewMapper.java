package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.ReviewResponse;
import com.surplusfood.marketplace.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId(),
                review.getConsumer().getId(),
                review.getConsumer().getFullName(),
                review.getBusiness().getId(),
                review.getBusiness().getBusinessName(),
                review.getOrder().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
