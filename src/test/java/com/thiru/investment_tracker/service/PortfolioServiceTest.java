package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.RedriveResult;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ProfitAndLossService profitAndLossService;

    @Mock
    private MongoTemplateService mongoTemplateService;

    @Mock
    private ReportService reportService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TemporaryTransactionService temporaryTransactionService;

    private TestablePortfolioService portfolioService;
    private UserMail userMail;

    @BeforeEach
    void setUp() {
        portfolioService = new TestablePortfolioService(
                portfolioRepository,
                transactionService,
                profitAndLossService,
                mongoTemplateService,
                reportService,
                transactionRepository,
                temporaryTransactionService
        );
        userMail = UserMail.from("test@example.com");
    }

    /**
     * A testable subclass of PortfolioService that allows overriding
     * the behaviour of methods that are difficult to mock in tests.
     */
    static class TestablePortfolioService extends PortfolioService {
        private int callCount;
        private int failFromCall;
        private RuntimeException throwOnCall;

        TestablePortfolioService(
                PortfolioRepository portfolioRepository,
                TransactionService transactionService,
                ProfitAndLossService profitAndLossService,
                MongoTemplateService mongoTemplateService,
                ReportService reportService,
                TransactionRepository transactionRepository,
                TemporaryTransactionService temporaryTransactionService) {
            super(portfolioRepository, transactionService, profitAndLossService,
                    mongoTemplateService, reportService, transactionRepository,
                    temporaryTransactionService);
        }

        void resetAddTransactionBehaviour() {
            this.callCount = 0;
            this.failFromCall = -1;
            this.throwOnCall = null;
        }

        /**
         * Call N (1-based) will throw the given exception. Calls before N succeed.
         */
        void setAddTransactionThrowsOnCall(int callNumber, RuntimeException ex) {
            this.failFromCall = callNumber;
            this.throwOnCall = ex;
        }

        @Override
        public String addTransaction(UserMail userMail, AssetRequest assetRequest, List<String> filteredOutTransactions) {
            callCount++;
            if (callCount == failFromCall && throwOnCall != null) {
                throw throwOnCall;
            }
            // itemFiltered stays empty → adds to succeeded
            return "Stock buy added to portfolio";
        }
    }

    @Test
    void redrive_allSucceed() {
        // Given: 3 temp transactions, none filtered
        List<TransactionEntity> tempTransactions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TransactionEntity txn = new TransactionEntity();
            txn.setId("temp-id-" + i);
            txn.setEmail(userMail.getEmail());
            txn.setAssetRequest(createAssetRequest("STOCK" + i));
            tempTransactions.add(txn);
        }

        when(temporaryTransactionService.getAllTemporaryTransactions(userMail))
                .thenReturn(tempTransactions);
        when(temporaryTransactionService.filterOutTransaction(any(), any(), eq(true)))
                .thenReturn(false);

        // When
        RedriveResult result = portfolioService.redriveTemporaryTransactions(userMail);

        // Then
        assertEquals(3, result.getSucceeded().size());
        assertTrue(result.getSucceeded().contains("temp-id-1"));
        assertTrue(result.getSucceeded().contains("temp-id-2"));
        assertTrue(result.getSucceeded().contains("temp-id-3"));
        assertTrue(result.getFailed().isEmpty());
        assertTrue(result.getStillFiltered().isEmpty());
        assertTrue(result.getFilteredOut().isEmpty());

        // Verify transactionRepository.saveAll was called once
        verify(transactionRepository).findAllById(result.getSucceeded());
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void redrive_allStillFiltered() {
        // Given: 3 temp transactions, all still filtered by corporate action
        List<TransactionEntity> tempTransactions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TransactionEntity txn = new TransactionEntity();
            txn.setId("temp-id-" + i);
            txn.setEmail(userMail.getEmail());
            txn.setAssetRequest(createAssetRequest("STOCK" + i));
            tempTransactions.add(txn);
        }

        when(temporaryTransactionService.getAllTemporaryTransactions(userMail))
                .thenReturn(tempTransactions);
        // All filtered - checkCorporateAction=true returns true
        when(temporaryTransactionService.filterOutTransaction(any(), any(), eq(true)))
                .thenReturn(true);

        // When
        RedriveResult result = portfolioService.redriveTemporaryTransactions(userMail);

        // Then
        assertTrue(result.getSucceeded().isEmpty());
        assertTrue(result.getFailed().isEmpty());
        assertEquals(3, result.getStillFiltered().size());
        assertTrue(result.getStillFiltered().contains("temp-id-1"));
        assertTrue(result.getStillFiltered().contains("temp-id-2"));
        assertTrue(result.getStillFiltered().contains("temp-id-3"));
        assertTrue(result.getFilteredOut().isEmpty());

        // No saves should happen since nothing succeeded
        verify(transactionRepository, never()).saveAll(anyList());
    }

    private AssetRequest createAssetRequest(String stockCode) {
        AssetRequest request = new AssetRequest();
        request.setStockCode(stockCode);
        request.setBrokerName(BrokerName.ZERODHA);
        request.setTransactionType(TransactionType.BUY);
        request.setPrice(100.0);
        request.setQuantity(10.0);
        request.setTransactionDate(LocalDate.now());
        request.setEmail(userMail.getEmail());
        return request;
    }
}
