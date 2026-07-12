package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.NotificationResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.Notification;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.repository.NotificationRepository;
import com.surplusfood.marketplace.util.PageMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponse sendNotification(User user, String title, String messageContent, NotificationType type) {
        log.info("Creating notification of type {} for user {}", type, user.getEmail());

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(messageContent);
        notification.setType(type);
        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = mapToResponse(saved);

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    response
            );
            log.info("Dispatched WebSocket notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to dispatch WebSocket message: {}", e.getMessage());
        }

        String emailBody = String.format("Hello %s,\n\n%s\n\nBest regards,\nSurplus Food Marketplace Team", user.getFullName(), messageContent);
        emailService.sendEmail(user.getEmail(), title, emailBody);

        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageMapper.toResponse(page, this::mapToResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this notification");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadAtIsNull(userId);
        Instant now = Instant.now();
        unread.forEach(n -> n.setReadAt(now));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUser().getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
