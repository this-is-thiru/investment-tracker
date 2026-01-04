package com.thiru.investment_tracker.entity.test;

import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;

@Document(value = "entity_test_2")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntityTest2 implements AuditableEntity {

    @MongoId
    private String id;

    @Field("email")
    private String email;

    @Field("transaction_date_1")
    private LocalDate transactionDate;

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private AuditMetadata auditMetadata = new AuditMetadata();
}
