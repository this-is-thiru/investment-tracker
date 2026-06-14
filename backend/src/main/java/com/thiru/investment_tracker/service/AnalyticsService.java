package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.analytics.AssetAllocationResponse;
import com.thiru.investment_tracker.dto.analytics.PerformanceMetricsResponse;
import com.thiru.investment_tracker.dto.analytics.PortfolioSummaryResponse;
import com.thiru.investment_tracker.dto.analytics.XirrRequest;
import com.thiru.investment_tracker.dto.analytics.XirrResponse;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.repository.TradeOutcomeRepository;
import com.thiru.investment_tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final ProfitAndLossRepository profitAndLossRepository;
    private final TradeOutcomeRepository tradeOutcomeRepository;

    public PortfolioSummaryResponse getPortfolioSummary(UserMail userMail) {
        // Stub
        return PortfolioSummaryResponse.builder().build();
    }

    public List<AssetAllocationResponse> getAssetAllocation(UserMail userMail) {
        // Stub
        return List.of();
    }

    public PerformanceMetricsResponse getPerformanceMetrics(UserMail userMail) {
        // Stub
        return PerformanceMetricsResponse.builder().build();
    }

    public XirrResponse calculateXirr(UserMail userMail, XirrRequest request) {
        // Stub
        return XirrResponse.builder().build();
    }
}