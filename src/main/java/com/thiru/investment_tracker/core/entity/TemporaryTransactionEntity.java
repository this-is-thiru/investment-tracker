package com.thiru.investment_tracker.core.entity;

import com.thiru.investment_tracker.core.dto.AssetRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@Document(value = "temporary_transactions")
public class TemporaryTransactionEntity extends TransactionEntity {
    AssetRequest assetRequest;
}
