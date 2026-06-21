package com.thiru.wealthlens.dto.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.wealthlens.entity.helper.AuditMetadata;
import com.thiru.wealthlens.util.time.TLocalDateTime;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditMetadataDto extends AuditMetadata {

    @JsonFormat(pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    @Override
    public LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    @JsonFormat(pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    @Override
    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }
}
