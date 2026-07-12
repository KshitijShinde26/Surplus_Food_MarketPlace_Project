package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.TransactionResponse;
import com.surplusfood.marketplace.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction t) {
        if (t == null) {
            return null;
        }
        return new TransactionResponse(
                t.getId(),
                t.getBusiness().getId(),
                t.getBusiness().getBusinessName(),
                t.getOrder() != null ? t.getOrder().getId() : null,
                t.getDonation() != null ? t.getDonation().getId() : null,
                t.getTransactionType(),
                t.getAmount(),
                t.getStatus(),
                t.getCreatedAt()
        );
    }
}
