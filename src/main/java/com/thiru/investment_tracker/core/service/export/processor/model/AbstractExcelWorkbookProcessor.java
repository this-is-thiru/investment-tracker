package com.thiru.investment_tracker.core.service.export.processor.model;

import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.entity.model.AuditableEntity;
import com.thiru.investment_tracker.core.service.export.writer.model.ExcelWorkbookWriter;
import com.thiru.investment_tracker.core.file.FileStream;
import com.thiru.investment_tracker.core.file.FileType;
import com.thiru.investment_tracker.core.util.time.TLocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;

import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractExcelWorkbookProcessor<EntityType extends AuditableEntity> implements ExcelWorkbookProcessor {

    private static final String FILE_NAME_DATE_FORMAT = "dd-MMMM-yyyy--hh-mm-ss-a";
    private final String fileName;
    private final FileType fileType;
    @Getter(value = AccessLevel.PROTECTED)
    private final UserMail userMail;

    public AbstractExcelWorkbookProcessor(UserMail userMail, String fileName, FileType fileType) {
        this.userMail = userMail;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    protected abstract ExcelWorkbookWriter<EntityType> workbookWriter();

    protected abstract List<EntityType> entities();

    @Override
    public FileStream fileStream() {
        InputStreamResource inputStreamResource = workbookWriter().process(this.entities());
        return FileStream.from(fileName(), inputStreamResource, fileType);
    }

    protected String fileName() {
        String fileNameDateComponent = TLocalDateTime.format(LocalDateTime.now(), FILE_NAME_DATE_FORMAT);
        return this.fileName + fileNameDateComponent + fileType.getExtension();
    }
}
