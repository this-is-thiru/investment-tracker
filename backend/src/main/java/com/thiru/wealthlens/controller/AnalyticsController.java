package com.thiru.wealthlens.controller;

import com.thiru.wealthlens.dto.analytics.AssetAllocationResponse;
import com.thiru.wealthlens.dto.analytics.PerformanceMetricsResponse;
import com.thiru.wealthlens.dto.analytics.PortfolioSummaryResponse;
import com.thiru.wealthlens.dto.analytics.XirrRequest;
import com.thiru.wealthlens.dto.analytics.XirrResponse;
import com.thiru.wealthlens.dto.user.UserMail;
import com.thiru.wealthlens.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics/")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("user/{email}/portfolio-summary")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary(@PathVariable String email) {
        return ResponseEntity.ok(analyticsService.getPortfolioSummary(UserMail.from(email)));
    }

    @GetMapping("user/{email}/asset-allocation")
    public ResponseEntity<List<AssetAllocationResponse>> getAssetAllocation(@PathVariable String email) {
        return ResponseEntity.ok(analyticsService.getAssetAllocation(UserMail.from(email)));
    }

    @GetMapping("user/{email}/performance-metrics")
    public ResponseEntity<PerformanceMetricsResponse> getPerformanceMetrics(@PathVariable String email) {
        return ResponseEntity.ok(analyticsService.getPerformanceMetrics(UserMail.from(email)));
    }

    @PostMapping("user/{email}/xirr")
    public ResponseEntity<XirrResponse> calculateXirr(@PathVariable String email, @RequestBody XirrRequest request) {
        return ResponseEntity.ok(analyticsService.calculateXirr(UserMail.from(email), request));
    }
}
