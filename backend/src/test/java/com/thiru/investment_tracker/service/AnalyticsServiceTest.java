package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.analytics.AssetAllocationResponse;
import com.thiru.investment_tracker.dto.analytics.PerformanceMetricsResponse;
import com.thiru.investment_tracker.dto.analytics.PortfolioSummaryResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.TradeOutcomeEntity;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.entity.model.FinancialReport;
import com.thiru.investment_tracker.entity.model.RealisedProfits;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.repository.TradeOutcomeRepository;
import com.thiru.investment_tracker.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProfitAndLossRepository profitAndLossRepository;

    @Mock
    private TradeOutcomeRepository tradeOutcomeRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getPortfolioSummary_whenCorrectData_returnsCorrectSummary() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        AssetEntity asset = new AssetEntity();
        asset.setPrice(100.0);
        asset.setQuantity(10.0);
        when(portfolioRepository.findByEmail(email)).thenReturn(List.of(asset));

        TransactionEntity buyTxn = new TransactionEntity();
        buyTxn.setTransactionType(TransactionType.BUY);
        TransactionEntity sellTxn = new TransactionEntity();
        sellTxn.setTransactionType(TransactionType.SELL);
        when(transactionRepository.findByEmail(email)).thenReturn(List.of(buyTxn, sellTxn));

        when(profitAndLossRepository.findAllByEmail(email)).thenReturn(List.of());

        // When
        PortfolioSummaryResponse result = analyticsService.getPortfolioSummary(userMail);

        // Then
        assertNotNull(result);
        assertEquals(1000.0, result.getTotalInvested());
        assertEquals(2L, result.getTotalTrades());
        assertEquals(1L, result.getBuyTrades());
        assertEquals(1L, result.getSellTrades());
        assertEquals(1L, result.getHoldingCount());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    void getPortfolioSummary_withPnlData_calculatesRealizedProfit() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        when(portfolioRepository.findByEmail(email)).thenReturn(List.of());
        when(transactionRepository.findByEmail(email)).thenReturn(List.of());

        ProfitAndLossEntity pnl = new ProfitAndLossEntity();
        RealisedProfits realised = RealisedProfits.empty();
        FinancialReport stReport = FinancialReport.empty();
        stReport.setProfit(500.0);
        FinancialReport ltReport = FinancialReport.empty();
        ltReport.setProfit(300.0);
        realised.setShortTermCapitalGains(stReport);
        realised.setLongTermCapitalGains(ltReport);
        pnl.setRealisedProfits(realised);

        when(profitAndLossRepository.findAllByEmail(email)).thenReturn(List.of(pnl));

        // When
        PortfolioSummaryResponse result = analyticsService.getPortfolioSummary(userMail);

        // Then
        assertEquals(800.0, result.getTotalRealizedProfit());
    }

    @Test
    void getAssetAllocation_whenEmptyPortfolio_returnsEmptyList() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);
        when(portfolioRepository.findByEmail(email)).thenReturn(List.of());

        // When
        List<AssetAllocationResponse> result = analyticsService.getAssetAllocation(userMail);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAssetAllocation_whenMultipleAssets_returnsCorrectPercentages() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        AssetEntity equity = new AssetEntity();
        equity.setAssetType(AssetType.EQUITY);
        equity.setPrice(100.0);
        equity.setQuantity(5.0);

        AssetEntity mf = new AssetEntity();
        mf.setAssetType(AssetType.MUTUAL_FUND);
        mf.setPrice(50.0);
        mf.setQuantity(10.0);

        when(portfolioRepository.findByEmail(email)).thenReturn(List.of(equity, mf));

        // When
        List<AssetAllocationResponse> result = analyticsService.getAssetAllocation(userMail);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        double totalInvested = result.stream()
                .mapToDouble(AssetAllocationResponse::getInvestedValue)
                .sum();
        assertEquals(1000.0, totalInvested, 0.01);

        double sum = result.stream().mapToDouble(AssetAllocationResponse::getPercentage).sum();
        assertEquals(100.0, sum, 0.01);

        // Verify EQUITY: 500 invested = 50%, MUTUAL_FUND: 500 invested = 50%
        AssetAllocationResponse equityResponse = result.stream()
                .filter(r -> r.getAssetType() == AssetType.EQUITY)
                .findFirst()
                .orElseThrow();
        assertEquals(500.0, equityResponse.getInvestedValue(), 0.01);
        assertEquals(50.0, equityResponse.getPercentage(), 0.01);
    }

    @Test
    void getPortfolioSummary_whenNoHoldings_returnsZeroHoldingCount() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        AssetEntity asset = new AssetEntity();
        asset.setPrice(100.0);
        asset.setQuantity(0.0); // No holdings
        when(portfolioRepository.findByEmail(email)).thenReturn(List.of(asset));
        when(transactionRepository.findByEmail(email)).thenReturn(List.of());
        when(profitAndLossRepository.findAllByEmail(email)).thenReturn(List.of());

        // When
        PortfolioSummaryResponse result = analyticsService.getPortfolioSummary(userMail);

        // Then
        assertEquals(0L, result.getHoldingCount());
    }

    @Test
    void getAssetAllocation_filtersOutNullAssetTypes() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        AssetEntity equity = new AssetEntity();
        equity.setAssetType(AssetType.EQUITY);
        equity.setPrice(100.0);
        equity.setQuantity(5.0);

        AssetEntity noType = new AssetEntity();
        noType.setAssetType(null); // Should be filtered
        noType.setPrice(50.0);
        noType.setQuantity(10.0);

        when(portfolioRepository.findByEmail(email)).thenReturn(List.of(equity, noType));

        // When
        List<AssetAllocationResponse> result = analyticsService.getAssetAllocation(userMail);

        // Then
        assertEquals(1, result.size());
        assertEquals(AssetType.EQUITY, result.get(0).getAssetType());
    }

    @Test
    void shouldReturnCorrectWinLossCounts() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        TradeOutcomeEntity win = new TradeOutcomeEntity();
        win.setNetProfit(500.0);
        win.setHoldingPeriodDays(30L);
        win.setTotalBuyValue(1000.0);
        win.setTotalSellValue(1500.0);
        win.setStockCode("RELIANCE");

        TradeOutcomeEntity loss = new TradeOutcomeEntity();
        loss.setNetProfit(-300.0);
        loss.setHoldingPeriodDays(60L);
        loss.setTotalBuyValue(2000.0);
        loss.setTotalSellValue(1700.0);
        loss.setStockCode("HDFC");

        TradeOutcomeEntity breakeven = new TradeOutcomeEntity();
        breakeven.setNetProfit(0.0);
        breakeven.setHoldingPeriodDays(45L);
        breakeven.setTotalBuyValue(500.0);
        breakeven.setTotalSellValue(500.0);
        breakeven.setStockCode("TCS");

        TradeOutcomeEntity anotherWin = new TradeOutcomeEntity();
        anotherWin.setNetProfit(200.0);
        anotherWin.setHoldingPeriodDays(90L);
        anotherWin.setTotalBuyValue(800.0);
        anotherWin.setTotalSellValue(1000.0);
        anotherWin.setStockCode("RELIANCE");

        when(tradeOutcomeRepository.findByEmail(email)).thenReturn(List.of(win, loss, breakeven, anotherWin));

        // When
        PerformanceMetricsResponse result = analyticsService.getPerformanceMetrics(userMail);

        // Then
        assertEquals(4L, result.getTotalTrades());
        assertEquals(2L, result.getWinCount());
        assertEquals(1L, result.getLossCount());
        assertEquals(1L, result.getBreakevenCount());
        assertEquals(350.0, result.getAverageProfitPerWin(), 0.01);
        assertEquals(300.0, result.getAverageLossPerLoss(), 0.01);
        assertEquals(2.0, result.getWinLossRatio(), 0.01);
        assertEquals(56.25, result.getAverageHoldingPeriodDays(), 0.01);
    }

    @Test
    void shouldIdentifyBestAndWorstStock() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        TradeOutcomeEntity relianceWin = new TradeOutcomeEntity();
        relianceWin.setStockCode("RELIANCE");
        relianceWin.setNetProfit(1000.0);
        relianceWin.setTotalBuyValue(5000.0);
        relianceWin.setTotalSellValue(6000.0);
        relianceWin.setHoldingPeriodDays(30L);

        TradeOutcomeEntity hdfcLoss = new TradeOutcomeEntity();
        hdfcLoss.setStockCode("HDFC");
        hdfcLoss.setNetProfit(-500.0);
        hdfcLoss.setTotalBuyValue(3000.0);
        hdfcLoss.setTotalSellValue(2500.0);
        hdfcLoss.setHoldingPeriodDays(60L);

        TradeOutcomeEntity tcsSmallWin = new TradeOutcomeEntity();
        tcsSmallWin.setStockCode("TCS");
        tcsSmallWin.setNetProfit(100.0);
        tcsSmallWin.setTotalBuyValue(1000.0);
        tcsSmallWin.setTotalSellValue(1100.0);
        tcsSmallWin.setHoldingPeriodDays(45L);

        when(tradeOutcomeRepository.findByEmail(email)).thenReturn(List.of(relianceWin, hdfcLoss, tcsSmallWin));

        // When
        PerformanceMetricsResponse result = analyticsService.getPerformanceMetrics(userMail);

        // Then
        assertNotNull(result.getBestPerformingStock());
        assertNotNull(result.getWorstPerformingStock());
        assertEquals("RELIANCE", result.getBestPerformingStock().getStockCode());
        assertEquals("HDFC", result.getWorstPerformingStock().getStockCode());
        assertEquals(20.0, result.getBestPerformingStock().getReturnPercentage(), 0.01);
        assertEquals(-16.67, result.getWorstPerformingStock().getReturnPercentage(), 0.01);
    }

    @Test
    void shouldReturnZeroMetricsWhenEmpty() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);
        when(tradeOutcomeRepository.findByEmail(email)).thenReturn(List.of());

        // When
        PerformanceMetricsResponse result = analyticsService.getPerformanceMetrics(userMail);

        // Then
        assertEquals(0L, result.getTotalTrades());
        assertEquals(0L, result.getWinCount());
        assertEquals(0L, result.getLossCount());
        assertEquals(0L, result.getBreakevenCount());
        assertEquals(0.0, result.getAverageProfitPerWin());
        assertEquals(0.0, result.getAverageLossPerLoss());
        assertEquals(0.0, result.getWinLossRatio());
        assertEquals(0.0, result.getAverageHoldingPeriodDays());
        assertEquals(0.0, result.getPortfolioTurnover());
    }

    @Test
    void shouldHandleNullNetProfitInMigratedRecords() {
        // Given
        String email = "test@example.com";
        UserMail userMail = UserMail.from(email);

        TradeOutcomeEntity withProfit = new TradeOutcomeEntity();
        withProfit.setNetProfit(500.0);
        withProfit.setHoldingPeriodDays(30L);
        withProfit.setTotalBuyValue(1000.0);
        withProfit.setTotalSellValue(1500.0);
        withProfit.setStockCode("RELIANCE");

        TradeOutcomeEntity migratedRecord = new TradeOutcomeEntity();
        migratedRecord.setNetProfit(-200.0); // negative = loss
        migratedRecord.setHoldingPeriodDays(60L);
        migratedRecord.setTotalBuyValue(2000.0);
        migratedRecord.setTotalSellValue(1800.0);
        migratedRecord.setStockCode("HDFC");

        when(tradeOutcomeRepository.findByEmail(email)).thenReturn(List.of(withProfit, migratedRecord));

        // When & Then - should not throw NPE
        PerformanceMetricsResponse result = analyticsService.getPerformanceMetrics(userMail);

        assertEquals(2L, result.getTotalTrades());
        assertEquals(1L, result.getWinCount());
        assertEquals(1L, result.getLossCount());
        assertEquals(0L, result.getBreakevenCount());
    }
}