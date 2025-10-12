package com.thiru.investment_tracker.core.entity.model;

import com.thiru.investment_tracker.core.entity.helper.AuditMetadata;

public interface AuditableEntity {
    AuditMetadata getAuditMetadata();
}
