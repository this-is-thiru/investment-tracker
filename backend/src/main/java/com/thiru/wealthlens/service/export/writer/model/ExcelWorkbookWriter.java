package com.thiru.wealthlens.service.export.writer.model;

import com.thiru.wealthlens.shared.entity.model.AuditableEntity;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

public interface ExcelWorkbookWriter<EntityType extends AuditableEntity> {

    InputStreamResource process(List<EntityType> entities);

}
