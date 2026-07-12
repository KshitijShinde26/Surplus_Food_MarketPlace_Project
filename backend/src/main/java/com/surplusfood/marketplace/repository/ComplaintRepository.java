package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Complaint;
import com.surplusfood.marketplace.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:businessId IS NULL OR c.business.id = :businessId)")
    Page<Complaint> searchComplaints(
            @Param("status") ComplaintStatus status,
            @Param("businessId") Long businessId,
            Pageable pageable
    );
}
