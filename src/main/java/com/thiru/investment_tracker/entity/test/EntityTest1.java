package com.thiru.investment_tracker.entity.test;

import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.entity.model.AuditableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Document(value = "entity_test_1")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntityTest1 implements AuditableEntity {

    @Id
    private String id;

    @Field("email")
    private String email;

    @Field("transaction_date")
    private LocalDate transactionDate;

    @Field("audit_metadata")
    @Setter(value = AccessLevel.NONE)
    private AuditMetadata auditMetadata = new AuditMetadata();
}
