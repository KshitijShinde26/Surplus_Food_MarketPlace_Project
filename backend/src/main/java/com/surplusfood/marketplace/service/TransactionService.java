package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.PageResponse;
import com.surplusfood.marketplace.dto.TransactionResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.Donation;
import com.surplusfood.marketplace.entity.Order;
import com.surplusfood.marketplace.entity.Transaction;
import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.TransactionMapper;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.TransactionRepository;
import com.surplusfood.marketplace.util.PageMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BusinessRepository businessRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public void logTransaction(
            Business business,
            Order order,
            Donation donation,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status
    ) {
        log.info("Logging transaction type: {} with status: {} for business: {}", type, status, business.getId());
        Transaction t = new Transaction();
        t.setBusiness(business);
        t.setOrder(order);
        t.setDonation(donation);
        t.setTransactionType(type);
        t.setAmount(amount);
        t.setStatus(status);
        transactionRepository.save(t);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getBusinessTransactions(Long ownerId, Pageable pageable) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        Page<Transaction> page = transactionRepository.findByBusinessIdOrderByCreatedAtDesc(business.getId(), pageable);
        return PageMapper.toResponse(page, transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSalesEarnings(Long ownerId) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        BigDecimal earnings = transactionRepository.calculateEarningsForBusiness(business.getId());
        return earnings != null ? earnings : BigDecimal.ZERO;
    }
}
