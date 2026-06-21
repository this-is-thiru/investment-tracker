package com.thiru.wealthlens.portfolio.api.event;

import com.thiru.wealthlens.portfolio.entity.TransactionEntity;

public record TransactionSavedEvent(TransactionEntity transaction) {
}
