package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CASE WHEN t.transactionType = com.surplusfood.marketplace.entity.TransactionType.SALE THEN t.amount ELSE -t.amount END) FROM Transaction t WHERE t.business.id = :businessId AND t.status = com.surplusfood.marketplace.entity.TransactionStatus.SUCCESS AND t.transactionType IN (com.surplusfood.marketplace.entity.TransactionType.SALE, com.surplusfood.marketplace.entity.TransactionType.REFUND)")
    java.math.BigDecimal calculateEarningsForBusiness(@org.springframework.data.repository.query.Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Query("SELECT MONTH(t.createdAt), SUM(CASE WHEN t.transactionType = com.surplusfood.marketplace.entity.TransactionType.SALE THEN t.amount ELSE -t.amount END) FROM Transaction t WHERE t.business.id = :businessId AND t.status = com.surplusfood.marketplace.entity.TransactionStatus.SUCCESS AND t.transactionType IN (com.surplusfood.marketplace.entity.TransactionType.SALE, com.surplusfood.marketplace.entity.TransactionType.REFUND) GROUP BY MONTH(t.createdAt)")
    java.util.List<Object[]> getMonthlyRevenueTrendForBusiness(@org.springframework.data.repository.query.Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Query("SELECT MONTH(t.createdAt), SUM(CASE WHEN t.transactionType = com.surplusfood.marketplace.entity.TransactionType.SALE THEN t.amount ELSE -t.amount END) FROM Transaction t WHERE t.status = com.surplusfood.marketplace.entity.TransactionStatus.SUCCESS AND t.transactionType IN (com.surplusfood.marketplace.entity.TransactionType.SALE, com.surplusfood.marketplace.entity.TransactionType.REFUND) GROUP BY MONTH(t.createdAt)")
    java.util.List<Object[]> getGlobalMonthlyRevenueTrend();
}
