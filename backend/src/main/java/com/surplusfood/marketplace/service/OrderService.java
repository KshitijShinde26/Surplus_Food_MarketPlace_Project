package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.OrderRequest;
import com.surplusfood.marketplace.dto.OrderResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.entity.ListingType;
import com.surplusfood.marketplace.entity.Order;
import com.surplusfood.marketplace.entity.OrderStatus;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.OrderMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.repository.OrderRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.util.PageMapper;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodListingRepository foodListingRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final TransactionService transactionService;

    @Transactional
    public OrderResponse placeOrder(Long consumerId, OrderRequest request) {
        User consumer = userRepository.findById(consumerId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer account not found"));

        FoodListing listing = foodListingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found"));

        if (listing.getStatus() != FoodListingStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Food listing is no longer active");
        }

        if (listing.getListingType() != ListingType.DISCOUNT_SALE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only food listings marked as Discount Sale can be purchased");
        }

        if (listing.getAvailableQuantity() < request.quantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Item already sold or not enough stock available");
        }

        listing.setAvailableQuantity(listing.getAvailableQuantity() - request.quantity());
        if (listing.getAvailableQuantity() == 0) {
            listing.setStatus(FoodListingStatus.SOLD_OUT);
        }
        foodListingRepository.save(listing);

        BigDecimal price = listing.getDiscountPrice() != null ? listing.getDiscountPrice() : listing.getOriginalPrice();
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(request.quantity()));

        Order order = new Order();
        order.setConsumer(consumer);
        order.setListing(listing);
        order.setQuantity(request.quantity());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPickupCode(generatePickupCode());

        Order saved = orderRepository.save(order);

        String msg = String.format("A new order of %d x '%s' has been placed by consumer %s. Payment is pending.",
                saved.getQuantity(), listing.getName(), consumer.getFullName());
        notificationService.sendNotification(
                listing.getBusiness().getOwner(),
                "New Order Received",
                msg,
                NotificationType.PAYMENT_UPDATE
        );

        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long consumerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByConsumerId(consumerId, pageable);
        return PageMapper.toResponse(orders, orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getBusinessOrders(Long ownerId, Pageable pageable) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        Page<Order> orders = orderRepository.findByListingBusinessId(business.getId(), pageable);
        return PageMapper.toResponse(orders, orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isConsumer = order.getConsumer().getId().equals(userId);
        boolean isBusinessOwner = order.getListing().getBusiness().getOwner().getId().equals(userId);

        if (!isConsumer && !isBusinessOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isConsumer = order.getConsumer().getId().equals(userId);
        boolean isBusinessOwner = order.getListing().getBusiness().getOwner().getId().equals(userId);

        if (!isConsumer && !isBusinessOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled in its current state: " + order.getStatus());
        }

        boolean wasPaid = order.getStatus() == OrderStatus.PAID;
        order.setStatus(OrderStatus.CANCELLED);

        FoodListing listing = order.getListing();
        listing.setAvailableQuantity(listing.getAvailableQuantity() + order.getQuantity());
        if (listing.getStatus() == FoodListingStatus.SOLD_OUT && listing.getAvailableQuantity() > 0) {
            listing.setStatus(FoodListingStatus.ACTIVE);
        }
        foodListingRepository.save(listing);

        Order saved = orderRepository.save(order);

        if (wasPaid) {
            transactionService.logTransaction(
                    order.getListing().getBusiness(),
                    order,
                    null,
                    TransactionType.REFUND,
                    order.getTotalAmount(),
                    TransactionStatus.SUCCESS
            );
        }

        User recipient = isConsumer ? order.getListing().getBusiness().getOwner() : order.getConsumer();
        String initiatorName = isConsumer ? order.getConsumer().getFullName() : "The business owner";
        String msg = String.format("Order #%d for %d x '%s' has been cancelled by %s.",
                order.getId(), order.getQuantity(), order.getListing().getName(), initiatorName);
        notificationService.sendNotification(
                recipient,
                "Order Cancelled",
                msg,
                NotificationType.PAYMENT_UPDATE
        );

        return orderMapper.toResponse(saved);
    }

    private String generatePickupCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
