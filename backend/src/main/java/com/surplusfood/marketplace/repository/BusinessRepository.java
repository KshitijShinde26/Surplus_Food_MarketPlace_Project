package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Business;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    @EntityGraph(attributePaths = "owner")
    Optional<Business> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    @Query("""
            select business
            from Business business
            join business.owner owner
            where (:verified is null or business.verified = :verified)
              and (
                :keyword is null
                or lower(business.businessName) like lower(concat('%', :keyword, '%'))
                or lower(owner.fullName) like lower(concat('%', :keyword, '%'))
                or lower(owner.email) like lower(concat('%', :keyword, '%'))
                or lower(business.city) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Business> searchForAdmin(
            @Param("verified") Boolean verified,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
