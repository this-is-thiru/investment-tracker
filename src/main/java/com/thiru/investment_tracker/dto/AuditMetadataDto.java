package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditMetadataDto {
    private String createdBy;
    private String updatedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TLocalDateTime.COMPLETE_DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
}