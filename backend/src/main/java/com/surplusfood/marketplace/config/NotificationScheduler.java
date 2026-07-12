package com.surplusfood.marketplace.config;

import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.PickupSchedule;
import com.surplusfood.marketplace.entity.PickupStatus;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.repository.PickupScheduleRepository;
import com.surplusfood.marketplace.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final FoodListingRepository foodListingRepository;
    private final PickupScheduleRepository pickupScheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processExpiredListings() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodListing> expiredListings = foodListingRepository.findExpiredListings(now);

        if (!expiredListings.isEmpty()) {
            log.info("Found {} expired food listings. Updating status.", expiredListings.size());
            for (FoodListing listing : expiredListings) {
                listing.setStatus(FoodListingStatus.EXPIRED);
                foodListingRepository.save(listing);

                String msg = String.format("Your surplus food listing '%s' has reached its expiry time and is now inactive.", listing.getName());
                notificationService.sendNotification(
                        listing.getBusiness().getOwner(),
                        "Food Listing Expired",
                        msg,
                        NotificationType.LISTING_EXPIRED
                );
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void remindUpcomingPickups() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<PickupSchedule> schedules = pickupScheduleRepository.findByStatusAndPickupTimeBetween(
                PickupStatus.SCHEDULED, now, oneHourLater);

        for (PickupSchedule schedule : schedules) {
            String notes = schedule.getNotes();
            if (notes != null && notes.contains("REMINDER_SENT")) {
                continue;
            }

            if (schedule.getOrder() != null) {
                // Remind Consumer
                String msg = String.format("Friendly reminder: Your pickup for '%s' is scheduled at %s. Pickup code: %s",
                        schedule.getOrder().getListing().getName(),
                        schedule.getPickupTime(),
                        schedule.getOrder().getPickupCode());
                notificationService.sendNotification(
                        schedule.getOrder().getConsumer(),
                        "Upcoming Order Pickup",
                        msg,
                        NotificationType.PICKUP_REMINDER
                );

                // Remind Business Owner
                String businessMsg = String.format("Friendly reminder: Consumer %s is scheduled to pick up order '%s' at %s.",
                        schedule.getOrder().getConsumer().getFullName(),
                        schedule.getOrder().getListing().getName(),
                        schedule.getPickupTime());
                notificationService.sendNotification(
                        schedule.getOrder().getListing().getBusiness().getOwner(),
                        "Upcoming Order Pickup",
                        businessMsg,
                        NotificationType.PICKUP_REMINDER
                );

            } else if (schedule.getDonation() != null) {
                // Remind NGO Representative
                String msg = String.format("Friendly reminder: Your NGO donation pickup for '%s' is scheduled at %s. Confirmation code: %s",
                        schedule.getDonation().getListing().getName(),
                        schedule.getPickupTime(),
                        schedule.getDonation().getConfirmationCode());
                notificationService.sendNotification(
                        schedule.getDonation().getNgo().getUser(),
                        "Upcoming Donation Pickup",
                        msg,
                        NotificationType.PICKUP_REMINDER
                );

                // Remind Business Owner
                String businessMsg = String.format("Friendly reminder: NGO %s is scheduled to pick up donation '%s' at %s.",
                        schedule.getDonation().getNgo().getOrganizationName(),
                        schedule.getDonation().getListing().getName(),
                        schedule.getPickupTime());
                notificationService.sendNotification(
                        schedule.getDonation().getListing().getBusiness().getOwner(),
                        "Upcoming Donation Pickup",
                        businessMsg,
                        NotificationType.PICKUP_REMINDER
                );
            }

            schedule.setNotes((notes == null ? "" : notes + " ") + "[REMINDER_SENT]");
            pickupScheduleRepository.save(schedule);
        }
    }
}
