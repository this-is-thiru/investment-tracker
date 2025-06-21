package com.thiru.investment_tracker.dto.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditMetadataDto extends AuditMetadata {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    @Override
    public LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    @Override
    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }
}
