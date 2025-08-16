package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.entity.LastlyPerformedCorporateAction;
import com.thiru.investment_tracker.entity.TemporaryTransactionEntity;
import com.thiru.investment_tracker.repository.CorporateActionRepository;
import com.thiru.investment_tracker.repository.LastlyPerformedCorporateActionRepo;
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

    private final CorporateActionRepository corporateActionRepository;
    private final TemporaryTransactionRepository temporaryTransactionRepository;
    private final LastlyPerformedCorporateActionRepo lastlyPerformedCorporateActionRepo;

    public Optional<String> filterOutTransaction(UserMail userMail, AssetRequest assetRequest) {

        LocalDate txnDate = assetRequest.getTransactionDate();
        if (anyCorporateActionToPerform(userMail, assetRequest.getStockCode(), txnDate)) {
            TemporaryTransactionEntity temporaryTransactionEntity = TObjectMapper.copy(assetRequest.asTransaction(), TemporaryTransactionEntity.class);
            temporaryTransactionEntity.setAssetRequest(assetRequest);
            temporaryTransactionEntity.setEmail(userMail.getEmail());
            TemporaryTransactionEntity savedEntity = temporaryTransactionRepository.save(temporaryTransactionEntity);
            return Optional.of(savedEntity.getId());
        }
        return Optional.empty();
    }

    public List<TemporaryTransactionEntity> getAllTemporaryTransactions(UserMail userMail) {
        return temporaryTransactionRepository.findByEmail(userMail.getEmail());
    }

    public boolean filterOutTransaction(UserMail userMail, TemporaryTransactionEntity temporaryTransaction) {
        LocalDate txnDate = temporaryTransaction.getTransactionDate();
        return anyCorporateActionToPerform(userMail, temporaryTransaction.getStockCode(), txnDate);
    }

    public boolean anyCorporateActionToPerform(UserMail userMail, String stockCode, LocalDate txnDate) {

        LocalDate quarterStart = getQuarterStart(txnDate);
        List<CorporateActionEntity> corporateActions = corporateActionRepository.findByStockCodeAndTypeInAndRecordDateBetween(stockCode, CorporateActionType.FILTERABLE_CORPORATE_ACTIONS, quarterStart, txnDate);

        for (CorporateActionEntity corporateAction : corporateActions) {
            if (isCorporateActionToPerform(userMail, corporateAction)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCorporateActionToPerform(UserMail userMail, CorporateActionEntity corporateAction) {

        String stockCode = corporateAction.getStockCode();
        CorporateActionType actionType = corporateAction.getType();
        AssetType assetType = corporateAction.getAssetType();

        Optional<LastlyPerformedCorporateAction> lastlyPerformedActionOptional = lastlyPerformedCorporateActionRepo
                .findByEmailAndStockCodeAndAssetTypeAndActionType(userMail.getEmail(), stockCode, assetType, actionType);
        Optional<LocalDate> nextDayOfActionDay = lastlyPerformedActionOptional.map(LastlyPerformedCorporateAction::getActionDate);
        return nextDayOfActionDay.map(actionDate -> actionDate.isBefore(corporateAction.getRecordDate())).orElse(true);
    }

    public void deleteTemporaryTransaction(UserMail userMail) {
        lastlyPerformedCorporateActionRepo.deleteByEmail(userMail.getEmail());
        log.info("Deleted lastly performed corporate actions for user: {}", userMail.getEmail());
        temporaryTransactionRepository.deleteByEmail(userMail.getEmail());
    }

    private static LocalDate getQuarterStart(LocalDate transactionDate) {
        int year = transactionDate.getYear();
        Month quarterStartMonth = transactionDate.getMonth().firstMonthOfQuarter();
        return LocalDate.of(year, quarterStartMonth, ONE);
    }
}
