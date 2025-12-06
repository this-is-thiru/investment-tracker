package com.thiru.investment_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.model.AuditMetadataDto;
import com.thiru.investment_tracker.dto.model.AuditableResponse;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.entity.model.DemergerDetail;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Data
public class CorporateActionDto implements AuditableResponse {
    private String id;
    private String stockCode;
    private String stockName;
    private String toStockCode;
    private String toStockName;
    private CorporateActionType type;
    private AssetType assetType = AssetType.EQUITY;
    private String description;
    private String actionPrice;
    private String ratio;
    private int priority;
    private DemergerDetailDto demergerDetail;

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
        corporateAction.setPriority(priority);
        corporateAction.setDemergerDetail(toDemergerDetail());

        return corporateAction;
    }

    private DemergerDetail toDemergerDetail() {
        var demergerStocks = demergerDetail.demergerStocks().stream().map(CorporateActionDto::toDemergerStock).toList();

        DemergerDetail detail = new DemergerDetail();
        detail.setDemergerRatio(demergerDetail.demergerRatio());
        detail.setDemergerPriceRatio(demergerDetail.demergerPriceRatio());
        detail.setMainStockCode(demergerDetail.mainStockCode());
        detail.setMainStockName(demergerDetail.mainStockName());
        detail.setDemergerStocks(demergerStocks);
        return detail;
    }

    private static DemergerDetail.DemergerStock toDemergerStock(CorporateActionDto.DemergerStockDto demergerStockDto) {
        var demergerStock = new DemergerDetail.DemergerStock();
        demergerStock.setStockCode(demergerStockDto.stockCode());
        demergerStock.setStockName(demergerStockDto.stockName());
        return demergerStock;
    }

    public record DemergerDetailDto(
            String demergerRatio,
            String demergerPriceRatio,
            String mainStockCode,
            String mainStockName,
            List<DemergerStockDto> demergerStocks
    ) {
    }

    public record DemergerStockDto(String stockCode, String stockName) {
    }
}
