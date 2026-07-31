package com.surplusfood.marketplace.listener;

import com.surplusfood.marketplace.dto.FoodListingResponse;
import com.surplusfood.marketplace.dto.NotificationResponse;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.entity.Wishlist;
import com.surplusfood.marketplace.event.FoodListingCreatedEvent;
import com.surplusfood.marketplace.event.NotificationCreatedEvent;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.repository.WishlistRepository;
import com.surplusfood.marketplace.service.EmailService;
import com.surplusfood.marketplace.service.NotificationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFoodListingCreated(FoodListingCreatedEvent event) {
        FoodListingResponse response = event.response();
        log.info("Processing FoodListingCreatedEvent asynchronously after transaction commit for listing: {}", response.name());

        // 1. Broadcast listing to WebSocket topic
        try {
            messagingTemplate.convertAndSend("/topic/listings", response);
            log.info("Successfully broadcasted food listing to WebSocket topic.");
        } catch (Exception e) {
            log.error("Failed to broadcast food listing: {}", e.getMessage(), e);
        }

        // 2. Identify nearby users and favorite-business wishlisters
        if (response.latitude() != null && response.longitude() != null) {
            try {
                List<User> nearbyUsers = userRepository.findNearbyConsumersAndNgos(
                        response.latitude().doubleValue(),
                        response.longitude().doubleValue(),
                        10.0
                );
                Set<Long> notifiedUserIds = nearbyUsers.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

                String nearbyMsg = String.format("A new surplus food listing '%s' is available near you from %s!",
                        response.name(), response.businessName());
                for (User user : nearbyUsers) {
                    notificationService.sendNotification(user, "New Food Nearby", nearbyMsg, NotificationType.NEW_FOOD_NEARBY);
                }

                List<Wishlist> wishlisters = wishlistRepository.findByBusinessId(response.businessId());
                String wishlistMsg = String.format("Your favorite business '%s' just posted a new listing: '%s'!",
                        response.businessName(), response.name());
                for (Wishlist w : wishlisters) {
                    User user = w.getUser();
                    if (!notifiedUserIds.contains(user.getId())) {
                        notificationService.sendNotification(user, "New Food from Favorite Business", wishlistMsg, NotificationType.NEW_FOOD_NEARBY);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process notifications for nearby/wishlist users: {}", e.getMessage(), e);
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        NotificationResponse response = event.response();
        String email = event.recipientEmail();
        String fullName = event.recipientFullName();

        log.info("Processing NotificationCreatedEvent asynchronously for user: {}", email);

        // 1. Dispatch WebSocket message to specific user
        try {
            messagingTemplate.convertAndSendToUser(
                    email,
                    "/queue/notifications",
                    response
            );
            log.info("Dispatched WebSocket notification to user: {}", email);
        } catch (Exception e) {
            log.error("Failed to dispatch WebSocket notification for {}: {}", email, e.getMessage(), e);
        }

        // 2. Send email notification
        try {
            String emailBody = String.format("Hello %s,\n\n%s\n\nBest regards,\nSurplus Food Marketplace Team",
                    fullName, response.message());
            emailService.sendEmail(email, response.title(), emailBody);
            log.info("Dispatched Email notification to user: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
        }
    }
}
