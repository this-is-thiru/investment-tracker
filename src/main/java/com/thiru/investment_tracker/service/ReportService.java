package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.context.ReportContext;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.ReportEntity;
import com.thiru.investment_tracker.repository.ReportRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void stockReport(UserMail userMail, ReportContext reportContext) {

        ReportEntity reportEntity = TObjectMapper.copy(reportContext, ReportEntity.class);
        reportEntity.setEmail(userMail.getEmail());

        reportRepository.save(reportEntity);
    }

    public List<ReportEntity> getStockReport(UserMail userMail) {

        return reportRepository.findByEmail(userMail.getEmail());
    }

    public void deleteReports(UserMail userMail) {
        reportRepository.deleteByEmail(userMail.getEmail());
    }

    public void updateReports() {
        List<ReportEntity> reportEntities = reportRepository.findAll();

        reportEntities.forEach(reportEntity -> {
                    AssetType assetType = reportEntity.getAssetType();
                    reportEntity.setAssetType(assetType == null ? AssetType.MUTUAL_FUND : assetType);
                }
        );
        reportRepository.saveAll(reportEntities);
    }
}
