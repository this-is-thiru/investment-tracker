package com.thiru.investment_tracker.entity.model;

import com.thiru.investment_tracker.entity.helper.AuditMetadata;

public interface AuditableEntity {
    AuditMetadata getAuditMetadata();
}
