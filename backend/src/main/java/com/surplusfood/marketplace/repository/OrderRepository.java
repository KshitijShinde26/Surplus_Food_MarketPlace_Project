package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByConsumerId(Long consumerId, Pageable pageable);
    Page<Order> findByListingBusinessId(Long businessId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.quantity), 0) FROM Order o WHERE o.listing.business.id = :businessId AND o.status IN (com.surplusfood.marketplace.entity.OrderStatus.PAID, com.surplusfood.marketplace.entity.OrderStatus.ACCEPTED, com.surplusfood.marketplace.entity.OrderStatus.READY_FOR_PICKUP, com.surplusfood.marketplace.entity.OrderStatus.COMPLETED)")
    long sumQuantityByBusinessIdAndPaidStatus(@org.springframework.data.repository.query.Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.quantity), 0) FROM Order o WHERE o.status IN (com.surplusfood.marketplace.entity.OrderStatus.PAID, com.surplusfood.marketplace.entity.OrderStatus.ACCEPTED, com.surplusfood.marketplace.entity.OrderStatus.READY_FOR_PICKUP, com.surplusfood.marketplace.entity.OrderStatus.COMPLETED)")
    long sumGlobalQuantityByPaidStatus();

    long countByStatusIn(java.util.Collection<com.surplusfood.marketplace.entity.OrderStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT MONTH(o.createdAt), COUNT(o) FROM Order o GROUP BY MONTH(o.createdAt)")
    java.util.List<Object[]> getMonthlyOrdersTrend();
}
