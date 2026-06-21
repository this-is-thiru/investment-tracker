package com.thiru.wealthlens.shared.entity.model;

import com.thiru.wealthlens.shared.entity.helper.AuditMetadata;

public interface AuditableEntity {
    AuditMetadata getAuditMetadata();
}
