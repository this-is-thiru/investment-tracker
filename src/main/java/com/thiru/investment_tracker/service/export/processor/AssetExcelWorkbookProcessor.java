package com.thiru.investment_tracker.service.export.processor;

import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.service.export.processor.model.AbstractExcelWorkbookProcessor;
import com.thiru.investment_tracker.service.export.writer.AssetExcelWorkbookWriter;
import com.thiru.investment_tracker.service.export.writer.model.ExcelWorkbookWriter;
import com.thiru.investment_tracker.helper.file.FileType;
import com.thiru.investment_tracker.service.PortfolioService;

import java.util.List;

public class AssetExcelWorkbookProcessor extends AbstractExcelWorkbookProcessor<AssetResponse> {

    private static final String ASSET_EXCEL_FILE_NAME = "portfolio-";
    private static final FileType FILE_TYPE = FileType.XLSX;

    private final PortfolioService portfolioService;
    private final List<String> columnFields;

    public AssetExcelWorkbookProcessor(UserMail userMail, List<String> columnFields, PortfolioService portfolioService) {
        super(userMail, ASSET_EXCEL_FILE_NAME, FILE_TYPE);
        this.columnFields = columnFields;
        this.portfolioService = portfolioService;
    }

    @Override
    protected ExcelWorkbookWriter<AssetResponse> workbookWriter() {
        return new AssetExcelWorkbookWriter("ASSETS", columnFields);
    }

    @Override
    protected List<AssetResponse> entities() {
        return portfolioService.getAllStocks(getUserMail());
    }
}
