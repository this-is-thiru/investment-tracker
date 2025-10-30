package com.thiru.investment_tracker.service.export.processor;

import com.thiru.investment_tracker.dto.response.AssetResponse;
import com.thiru.investment_tracker.dto.request.EntityExportRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.query.QueryFilter;
import com.thiru.investment_tracker.helper.file.FileType;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.export.processor.model.AbstractExcelWorkbookProcessor;
import com.thiru.investment_tracker.service.export.writer.AssetExcelWorkbookWriter;
import com.thiru.investment_tracker.service.export.writer.model.ExcelWorkbookWriter;

import java.util.List;

public class AssetExcelWorkbookProcessor extends AbstractExcelWorkbookProcessor<AssetResponse> {

    private static final String ASSET_EXCEL_FILE_NAME = "portfolio-";
    private static final FileType FILE_TYPE = FileType.XLSX;

    private final PortfolioService portfolioService;
    private final List<String> columnFields;
    private final List<QueryFilter> queryFilters;

    public AssetExcelWorkbookProcessor(UserMail userMail, EntityExportRequest exportRequest, PortfolioService portfolioService) {
        super(userMail, ASSET_EXCEL_FILE_NAME, FILE_TYPE);
        this.columnFields = exportRequest.getSelectedColumns();
        this.queryFilters = exportRequest.getFilters();
        this.portfolioService = portfolioService;
    }

    @Override
    protected ExcelWorkbookWriter<AssetResponse> workbookWriter() {
        return new AssetExcelWorkbookWriter("ASSETS", columnFields);
    }

    @Override
    protected List<AssetResponse> entities() {
        return portfolioService.getExportEntities(getUserMail() , queryFilters);
    }
}
