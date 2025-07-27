package com.thiru.investment_tracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@Document(value = "temporary_transactions")
public class TemporaryTransactionEntity extends TransactionEntity {

}
