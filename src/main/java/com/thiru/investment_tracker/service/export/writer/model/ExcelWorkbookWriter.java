package com.thiru.investment_tracker.service.export.writer.model;

import org.springframework.core.io.InputStreamResource;

import java.util.List;

public interface ExcelWorkbookWriter<Entity> {

    InputStreamResource process(List<Entity> entities);

}
