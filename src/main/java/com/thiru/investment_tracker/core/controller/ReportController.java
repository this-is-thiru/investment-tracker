package com.thiru.investment_tracker.core.controller;

import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.entity.ReportEntity;
import com.thiru.investment_tracker.core.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/reports/user/{email}")
@RestController
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public List<ReportEntity> getReports(@PathVariable String email) {
        return reportService.getStockReport(UserMail.from(email));
    }
}
