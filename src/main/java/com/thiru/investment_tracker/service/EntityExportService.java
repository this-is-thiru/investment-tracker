package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.EntityExportRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.helper.file.FileStream;
import com.thiru.investment_tracker.service.export.processor.AssetExcelWorkbookProcessor;
import com.thiru.investment_tracker.service.export.processor.TransactionExcelWorkbookProcessor;
import com.thiru.investment_tracker.service.export.processor.TransactionUploadTemplateProcessor;
import com.thiru.investment_tracker.service.export.processor.model.ExcelWorkbookProcessor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EntityExportService {

    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    public FileStream export(UserMail userMail, EntityExportRequest exportRequest) {
        String entityName = exportRequest.getEntityName();
        return switch (entityName) {
            case "assets" -> portfolioExport(userMail, exportRequest);
            case "transactions" -> transactionExport(userMail, exportRequest);
            case "transactions-template" -> transactionTemplate(userMail, exportRequest);
            default -> throw new IllegalArgumentException("Invalid entity name: " + entityName);
        };
    }

    private FileStream portfolioExport(UserMail userMail, EntityExportRequest exportRequest) {
        ExcelWorkbookProcessor processor = new AssetExcelWorkbookProcessor(userMail, exportRequest, portfolioService);
        return processor.fileStream();
    }

    private FileStream transactionExport(UserMail userMail, EntityExportRequest exportRequest) {
        ExcelWorkbookProcessor processor = new TransactionExcelWorkbookProcessor(userMail, exportRequest, transactionService);
        return processor.fileStream();
    }

    private FileStream transactionTemplate(UserMail userMail, EntityExportRequest exportRequest) {
        ExcelWorkbookProcessor processor = new TransactionUploadTemplateProcessor(userMail, exportRequest.getSelectedColumns());
        return processor.fileStream();
    }
}
