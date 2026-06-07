package com.thiru.investment_tracker.service.export.writer.model;

import com.thiru.investment_tracker.entity.model.AuditableEntity;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

public interface ExcelWorkbookWriter<EntityType extends AuditableEntity> {

    InputStreamResource process(List<EntityType> entities);

}
