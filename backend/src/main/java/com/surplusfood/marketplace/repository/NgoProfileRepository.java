package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.NgoProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NgoProfileRepository extends JpaRepository<NgoProfile, Long> {
    Optional<NgoProfile> findByUserId(Long userId);

    @Query("SELECT n FROM NgoProfile n WHERE " +
            "(:verified IS NULL OR n.verified = :verified) AND " +
            "(:keyword IS NULL OR LOWER(n.organizationName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<NgoProfile> searchNgoProfiles(
            @Param("verified") Boolean verified,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
