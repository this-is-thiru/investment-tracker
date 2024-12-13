package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.enums.AccountType;
import org.springframework.stereotype.Service;

import com.thiru.investment_tracker.dto.ReportContext;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.entity.Report;
import com.thiru.investment_tracker.repository.ReportRepository;
import com.thiru.investment_tracker.dto.user.UserMail;

import lombok.AllArgsConstructor;

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

    public void deleteReports(UserMail userMail) {
        reportRepository.deleteByEmail(userMail.getEmail());
    }

//    public void updateTransactions() {
//        List<Report> transactions = reportRepository.findAll();
//
//        transactions.forEach(transaction -> {
//                    AccountType accountType = transaction.getAccountType();
//                    transaction.setAccountType(accountType == AccountType.OUT_SOURCE ? AccountType.OUTSOURCED : accountType);
//                }
//        );
//        reportRepository.saveAll(transactions);
//    }
}
