package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.entity.TemporaryTransactionEntity;
import com.thiru.investment_tracker.repository.CorporateActionRepository;
import com.thiru.investment_tracker.repository.TemporaryTransactionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TemporaryTransactionService {

    private static final int ONE = 1;

    private final TemporaryTransactionRepository temporaryTransactionRepository;
    private final CorporateActionRepository corporateActionRepository;

    public Optional<String> filterOutTransaction(UserMail userMail, AssetRequest assetRequest) {
        LocalDate txnDate = assetRequest.getTransactionDate();
        if (isCorporateActionToPerform(assetRequest.getStockCode(), txnDate)) {
            TemporaryTransactionEntity temporaryTransactionEntity = TObjectMapper.copy(assetRequest.getTransaction(), TemporaryTransactionEntity.class);
            temporaryTransactionEntity.setEmail(userMail.getEmail());
            TemporaryTransactionEntity savedEntity = temporaryTransactionRepository.save(temporaryTransactionEntity);
            return Optional.of(savedEntity.getId());
        }
        return Optional.empty();
    }

    public boolean isCorporateActionToPerform(String stockCode, LocalDate transactionDate) {

        LocalDate quarterStart = getQuarterStart(transactionDate);
        List<CorporateActionEntity> corporateActions = corporateActionRepository.findByStockCodeAndTypeInAndRecordDateBetween(stockCode, CorporateActionType.FILTERABLE_CORPORATE_ACTIONS, quarterStart, transactionDate);
        return !corporateActions.isEmpty();
    }

    private static LocalDate getQuarterStart(LocalDate transactionDate) {
        int year = transactionDate.getYear();
        Month quarterStartMonth = transactionDate.getMonth().firstMonthOfQuarter();
        return LocalDate.of(year, quarterStartMonth, ONE);
    }
}
