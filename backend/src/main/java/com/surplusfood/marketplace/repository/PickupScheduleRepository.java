package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.PickupSchedule;
import com.surplusfood.marketplace.entity.PickupStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickupScheduleRepository extends JpaRepository<PickupSchedule, Long> {
    List<PickupSchedule> findByStatusAndPickupTimeBetween(PickupStatus status, LocalDateTime start, LocalDateTime end);
    Optional<PickupSchedule> findByOrderId(Long orderId);
    Optional<PickupSchedule> findByDonationId(Long donationId);
}
