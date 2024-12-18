package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.ReportContext;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Report;
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

        Report report = TObjectMapper.copy(reportContext, Report.class);
        report.setEmail(userMail.getEmail());

        reportRepository.save(report);
    }

    public List<Report> getStockReport(UserMail userMail) {

        return reportRepository.findByEmail(userMail.getEmail());
    }

    public void deleteReports(UserMail userMail) {
        reportRepository.deleteByEmail(userMail.getEmail());
    }

    public void updateReports() {
        List<Report> reports = reportRepository.findAll();

        reports.forEach(report -> {
                    AssetType assetType = report.getAssetType();
                    report.setAssetType(assetType == null ? AssetType.MUTUAL_FUND : assetType);
                }
        );
        reportRepository.saveAll(reports);
    }
}
