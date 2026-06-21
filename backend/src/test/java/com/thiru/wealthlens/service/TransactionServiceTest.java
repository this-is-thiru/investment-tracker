package com.thiru.wealthlens.service;

import com.thiru.wealthlens.dto.AssetRequest;
import com.thiru.wealthlens.dto.enums.BrokerName;
import com.thiru.wealthlens.dto.enums.TransactionType;
import com.thiru.wealthlens.shared.dto.user.UserMail;
import com.thiru.wealthlens.entity.TransactionEntity;
import com.thiru.wealthlens.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MongoTemplateService mongoTemplateService;

    @InjectMocks
    private TransactionService transactionService;

    private UserMail userMail;
    private AssetRequest assetRequest;

    @BeforeEach
    void setUp() {
        userMail = UserMail.from("test@example.com");
        assetRequest = new AssetRequest();
        assetRequest.setStockCode("AAPL");
        assetRequest.setBrokerName(BrokerName.ZERODHA);
        assetRequest.setTransactionType(TransactionType.BUY);
        assetRequest.setPrice(150.0);
        assetRequest.setQuantity(10.0);
        assetRequest.setTransactionDate(LocalDate.now());
    }

    @Test
    void addTransaction_withNullTempTransactionId_savesNormally() {
        assetRequest.setTempTransactionId(null);
        TransactionEntity saved = new TransactionEntity();
        saved.setId("new-id");
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(saved);

        String result = transactionService.addTransaction(userMail, assetRequest);

        assertEquals("new-id", result);
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void addTransaction_withNewTempTransactionId_savesNormally() {
        String tempId = "temp-123";
        assetRequest.setTempTransactionId(tempId);
        when(transactionRepository.findByEmailAndSourceTempTransactionId("test@example.com", tempId))
                .thenReturn(Optional.empty());
        TransactionEntity saved = new TransactionEntity();
        saved.setId("new-id");
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(saved);

        String result = transactionService.addTransaction(userMail, assetRequest);

        assertEquals("new-id", result);
        verify(transactionRepository).findByEmailAndSourceTempTransactionId("test@example.com", tempId);
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void addTransaction_withDuplicateTempTransactionId_returnsExistingIdWithoutSaving() {
        String tempId = "temp-123";
        assetRequest.setTempTransactionId(tempId);
        TransactionEntity existing = new TransactionEntity();
        existing.setId("existing-id");
        when(transactionRepository.findByEmailAndSourceTempTransactionId("test@example.com", tempId))
                .thenReturn(Optional.of(existing));

        String result = transactionService.addTransaction(userMail, assetRequest);

        assertEquals("existing-id", result);
        verify(transactionRepository).findByEmailAndSourceTempTransactionId("test@example.com", tempId);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }
}
