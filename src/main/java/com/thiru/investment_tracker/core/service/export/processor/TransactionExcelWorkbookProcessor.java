package com.thiru.investment_tracker.core.service.export.processor;

import com.thiru.investment_tracker.core.dto.EntityExportRequest;
import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.entity.TransactionEntity;
import com.thiru.investment_tracker.core.file.FileType;
import com.thiru.investment_tracker.core.service.TransactionService;
import com.thiru.investment_tracker.core.service.export.processor.model.AbstractExcelWorkbookProcessor;
import com.thiru.investment_tracker.core.service.export.writer.TransactionExcelWorkbookWriter;
import com.thiru.investment_tracker.core.service.export.writer.model.ExcelWorkbookWriter;

import java.util.List;

public class TransactionExcelWorkbookProcessor extends AbstractExcelWorkbookProcessor<TransactionEntity> {

    private static final String ASSET_EXCEL_FILE_NAME = "transaction-";
    private static final FileType FILE_TYPE = FileType.XLSX;

    private final TransactionService transactionService;
    private final List<String> columnFields;

    public TransactionExcelWorkbookProcessor(UserMail userMail, EntityExportRequest exportRequest, TransactionService transactionService) {
        super(userMail, ASSET_EXCEL_FILE_NAME, FILE_TYPE);
        this.columnFields = exportRequest.getSelectedColumns();
        this.transactionService = transactionService;
    }

    @Override
    protected ExcelWorkbookWriter<TransactionEntity> workbookWriter() {
        return new TransactionExcelWorkbookWriter("TRANSACTIONS", columnFields);
    }

    @Override
    protected List<TransactionEntity> entities() {
        return transactionService.getUserTransactions(getUserMail());
    }
}
