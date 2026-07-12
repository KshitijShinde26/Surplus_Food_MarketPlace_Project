package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.PickupScheduleResponse;
import com.surplusfood.marketplace.entity.Donation;
import com.surplusfood.marketplace.entity.Order;
import com.surplusfood.marketplace.entity.PickupSchedule;
import com.surplusfood.marketplace.entity.PickupStatus;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.repository.PickupScheduleRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PickupScheduleService {

    private final PickupScheduleRepository pickupScheduleRepository;

    @Transactional
    public PickupScheduleResponse createScheduleForOrder(Order order, LocalDateTime pickupTime) {
        PickupSchedule schedule = new PickupSchedule();
        schedule.setOrder(order);
        schedule.setPickupTime(pickupTime);
        schedule.setStatus(PickupStatus.SCHEDULED);
        PickupSchedule saved = pickupScheduleRepository.save(schedule);
        return mapToResponse(saved);
    }

    @Transactional
    public PickupScheduleResponse createScheduleForDonation(Donation donation, LocalDateTime pickupTime) {
        PickupSchedule schedule = new PickupSchedule();
        schedule.setDonation(donation);
        schedule.setPickupTime(pickupTime);
        schedule.setStatus(PickupStatus.SCHEDULED);
        PickupSchedule saved = pickupScheduleRepository.save(schedule);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PickupScheduleResponse getByOrderId(Long orderId) {
        PickupSchedule schedule = pickupScheduleRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup schedule not found for order"));
        return mapToResponse(schedule);
    }

    @Transactional(readOnly = true)
    public PickupScheduleResponse getByDonationId(Long donationId) {
        PickupSchedule schedule = pickupScheduleRepository.findByDonationId(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup schedule not found for donation"));
        return mapToResponse(schedule);
    }

    @Transactional
    public PickupScheduleResponse updateStatus(Long id, PickupStatus status) {
        PickupSchedule schedule = pickupScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup schedule not found"));
        schedule.setStatus(status);
        PickupSchedule saved = pickupScheduleRepository.save(schedule);
        return mapToResponse(saved);
    }

    private PickupScheduleResponse mapToResponse(PickupSchedule schedule) {
        return new PickupScheduleResponse(
                schedule.getId(),
                schedule.getOrder() != null ? schedule.getOrder().getId() : null,
                schedule.getDonation() != null ? schedule.getDonation().getId() : null,
                schedule.getPickupTime(),
                schedule.getStatus(),
                schedule.getNotes()
        );
    }
}
