package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.FoodListing;
import com.surplusfood.marketplace.entity.FoodListingStatus;
import com.surplusfood.marketplace.entity.ListingType;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    Page<FoodListing> findByBusinessIdAndStatusNot(Long businessId, FoodListingStatus status, Pageable pageable);

    @Query(value = "SELECT *, " +
            "(6371 * acos(LEAST(1.0, GREATEST(-1.0, cos(radians(:latitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(latitude)))))) AS distance " +
            "FROM food_listings " +
            "WHERE status = 'ACTIVE' " +
            "AND expiry_time > :now " +
            "AND (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:listingType IS NULL OR listing_type = :listingType) " +
            "AND (:vegetarian IS NULL OR vegetarian = :vegetarian) " +
            "AND (:vegan IS NULL OR vegan = :vegan) " +
            "AND (:keyword IS NULL OR (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%'))) " +
            "HAVING distance <= :radius " +
            "ORDER BY distance ASC",
            countQuery = "SELECT count(*) FROM food_listings " +
                    "WHERE status = 'ACTIVE' " +
                    "AND expiry_time > :now " +
                    "AND (:categoryId IS NULL OR category_id = :categoryId) " +
                    "AND (:listingType IS NULL OR listing_type = :listingType) " +
                    "AND (:vegetarian IS NULL OR vegetarian = :vegetarian) " +
                    "AND (:vegan IS NULL OR vegan = :vegan) " +
                    "AND (:keyword IS NULL OR (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%'))) " +
                    "AND (6371 * acos(LEAST(1.0, GREATEST(-1.0, cos(radians(:latitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(latitude)))))) <= :radius",
            nativeQuery = true)
    Page<FoodListing> findNearbyActiveListings(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") double radius,
            @Param("categoryId") Long categoryId,
            @Param("listingType") String listingType,
            @Param("vegetarian") Boolean vegetarian,
            @Param("vegan") Boolean vegan,
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query(value = "SELECT * FROM food_listings " +
            "WHERE status = 'ACTIVE' " +
            "AND expiry_time > :now " +
            "AND (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:listingType IS NULL OR listing_type = :listingType) " +
            "AND (:vegetarian IS NULL OR vegetarian = :vegetarian) " +
            "AND (:vegan IS NULL OR vegan = :vegan) " +
            "AND (:keyword IS NULL OR (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT count(*) FROM food_listings " +
                    "WHERE status = 'ACTIVE' " +
                    "AND expiry_time > :now " +
                    "AND (:categoryId IS NULL OR category_id = :categoryId) " +
                    "AND (:listingType IS NULL OR listing_type = :listingType) " +
                    "AND (:vegetarian IS NULL OR vegetarian = :vegetarian) " +
                    "AND (:vegan IS NULL OR vegan = :vegan) " +
                    "AND (:keyword IS NULL OR (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    Page<FoodListing> findAllActiveListings(
            @Param("categoryId") Long categoryId,
            @Param("listingType") String listingType,
            @Param("vegetarian") Boolean vegetarian,
            @Param("vegan") Boolean vegan,
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT f FROM FoodListing f WHERE f.status = com.surplusfood.marketplace.entity.FoodListingStatus.ACTIVE AND f.expiryTime <= :now")
    java.util.List<FoodListing> findExpiredListings(@Param("now") LocalDateTime now);

    @Query("SELECT MONTH(f.createdAt), COUNT(f) FROM FoodListing f GROUP BY MONTH(f.createdAt)")
    java.util.List<Object[]> getMonthlyListingsTrend();
}
