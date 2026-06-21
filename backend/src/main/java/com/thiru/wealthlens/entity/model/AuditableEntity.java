package com.thiru.wealthlens.entity.model;

import com.thiru.wealthlens.entity.helper.AuditMetadata;

public interface AuditableEntity {
    AuditMetadata getAuditMetadata();
}
