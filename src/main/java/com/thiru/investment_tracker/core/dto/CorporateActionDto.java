package com.thiru.investment_tracker.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.core.dto.enums.AssetType;
import com.thiru.investment_tracker.core.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.core.dto.model.AuditMetadataDto;
import com.thiru.investment_tracker.core.dto.model.AuditableResponse;
import com.thiru.investment_tracker.core.entity.CorporateActionEntity;
import com.thiru.investment_tracker.core.util.collection.TCollectionUtil;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CorporateActionDto implements AuditableResponse {
    private String id;
    private String stockCode;
    private String stockName;
    private String toStockCode;
    private String toStockName;
    private CorporateActionType type;
    private AssetType assetType;
    private String description;
    private String actionPrice;
    private String ratio;
    private int priority;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate exDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate recordDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
    private LocalDate date;

    private AuditMetadataDto auditMetadata;

    @JsonIgnore
    public CorporateActionEntity getAsEntity() {
        CorporateActionEntity corporateAction = new CorporateActionEntity();

        corporateAction.setStockCode(stockCode);
        corporateAction.setStockName(stockName);
        corporateAction.setToStockCode(toStockCode);
        corporateAction.setToStockName(toStockName);
        corporateAction.setType(type);
        corporateAction.setAssetType(assetType);
        corporateAction.setDescription(description);
        corporateAction.setActionPrice(actionPrice);
        corporateAction.setRatio(ratio);
        corporateAction.setExDate(exDate);
        corporateAction.setRecordDate(recordDate);

        return corporateAction;
    }
}
