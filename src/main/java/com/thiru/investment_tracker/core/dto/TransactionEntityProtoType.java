package com.thiru.investment_tracker.core.dto;

import com.thiru.investment_tracker.core.entity.TransactionEntity;

public interface TransactionEntityProtoType {
    TransactionEntity asTransaction();
}
