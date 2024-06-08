package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.ReportContext;
import com.thiru.investment_tracker.entity.Report;
import com.thiru.investment_tracker.repository.ReportRepository;
import com.thiru.investment_tracker.user.UserMail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;


    public void stockReport(UserMail userMail, ReportContext reportContext) {

        Report report = CommonUtil.copy(reportContext, Report.class);
        report.setEmail(userMail.getEmail());

        reportRepository.save(report);
    }
}
