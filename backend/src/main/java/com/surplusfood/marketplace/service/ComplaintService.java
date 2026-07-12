package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.ComplaintRequest;
import com.surplusfood.marketplace.dto.ComplaintResponse;
import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.Complaint;
import com.surplusfood.marketplace.entity.ComplaintStatus;
import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.ComplaintMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.ComplaintRepository;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final FoodListingRepository foodListingRepository;
    private final ComplaintMapper complaintMapper;

    @Transactional
    public ComplaintResponse fileComplaint(Long userId, ComplaintRequest request) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter user not found"));

        Business business = null;
        if (request.businessId() != null) {
            business = businessRepository.findById(request.businessId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target business profile not found"));
        }

        FoodListing listing = null;
        if (request.listingId() != null) {
            listing = foodListingRepository.findById(request.listingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target food listing not found"));
        }

        Complaint complaint = new Complaint();
        complaint.setReporter(reporter);
        complaint.setBusiness(business);
        complaint.setListing(listing);
        complaint.setSubject(request.subject());
        complaint.setDescription(request.description());
        complaint.setStatus(ComplaintStatus.OPEN);

        Complaint saved = complaintRepository.save(complaint);
        return complaintMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> getMyComplaints(Long userId, Pageable pageable) {
        Page<Complaint> page = complaintRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable);
        return PageMapper.toResponse(page, complaintMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> searchComplaints(ComplaintStatus status, Long businessId, Pageable pageable) {
        Page<Complaint> page = complaintRepository.searchComplaints(status, businessId, pageable);
        return PageMapper.toResponse(page, complaintMapper::toResponse);
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, ComplaintStatus status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        complaint.setStatus(status);
        Complaint saved = complaintRepository.save(complaint);
        return complaintMapper.toResponse(saved);
    }
}
