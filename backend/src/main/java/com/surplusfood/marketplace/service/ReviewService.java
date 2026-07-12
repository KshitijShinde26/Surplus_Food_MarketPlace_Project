package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.dto.ReviewRequest;
import com.surplusfood.marketplace.dto.ReviewResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.Order;
import com.surplusfood.marketplace.entity.OrderStatus;
import com.surplusfood.marketplace.entity.Review;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.ReviewMapper;
import com.surplusfood.marketplace.repository.OrderRepository;
import com.surplusfood.marketplace.repository.ReviewRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User consumer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getConsumer().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot review an order placed by another user");
        }

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.FAILED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can only review paid or picked up orders");
        }

        if (reviewRepository.findByOrderId(order.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already submitted a review for this order");
        }

        if (request.rating() < 1 || request.rating() > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Business business = order.getListing().getBusiness();

        Review review = new Review();
        review.setConsumer(consumer);
        review.setBusiness(business);
        review.setOrder(order);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getBusinessReviews(Long businessId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByBusinessId(businessId, pageable);
        return PageMapper.toResponse(page, reviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long businessId) {
        Double avg = reviewRepository.getAverageRatingForBusiness(businessId);
        return avg != null ? avg : 0.0;
    }
}
