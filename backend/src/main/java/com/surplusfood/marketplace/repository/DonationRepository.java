package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findByNgoId(Long ngoId, Pageable pageable);
    Page<Donation> findByListingBusinessId(Long businessId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d WHERE d.listing.business.id = :businessId AND d.status IN (com.surplusfood.marketplace.entity.DonationStatus.APPROVED, com.surplusfood.marketplace.entity.DonationStatus.PICKED_UP)")
    long sumQuantityByBusinessIdAndApprovedStatus(@org.springframework.data.repository.query.Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d WHERE d.status IN (com.surplusfood.marketplace.entity.DonationStatus.APPROVED, com.surplusfood.marketplace.entity.DonationStatus.PICKED_UP)")
    long sumGlobalQuantityByApprovedStatus();

    long countByListingBusinessId(Long businessId);
    long countByListingBusinessIdAndStatusIn(Long businessId, java.util.Collection<com.surplusfood.marketplace.entity.DonationStatus> statuses);

    long countByNgoId(Long ngoId);
    long countByNgoIdAndStatus(Long ngoId, com.surplusfood.marketplace.entity.DonationStatus status);
    long countByNgoIdAndStatusIn(Long ngoId, java.util.Collection<com.surplusfood.marketplace.entity.DonationStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d WHERE d.ngo.id = :ngoId AND d.status IN (com.surplusfood.marketplace.entity.DonationStatus.APPROVED, com.surplusfood.marketplace.entity.DonationStatus.PICKED_UP)")
    long sumQuantityByNgoIdAndApprovedStatus(@org.springframework.data.repository.query.Param("ngoId") Long ngoId);

    @org.springframework.data.jpa.repository.Query("SELECT MONTH(d.createdAt), COUNT(d) FROM Donation d WHERE d.listing.business.id = :businessId GROUP BY MONTH(d.createdAt)")
    java.util.List<Object[]> getMonthlyDonationsTrendForBusiness(@org.springframework.data.repository.query.Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Query("SELECT MONTH(d.createdAt), COUNT(d) FROM Donation d WHERE d.ngo.id = :ngoId GROUP BY MONTH(d.createdAt)")
    java.util.List<Object[]> getMonthlyClaimsTrendForNgo(@org.springframework.data.repository.query.Param("ngoId") Long ngoId);
}
