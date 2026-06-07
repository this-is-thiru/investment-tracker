package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CorporateActionRepository extends MongoRepository<CorporateActionEntity, String> {
    List<CorporateActionEntity> findByType(CorporateActionType type);

    /**
     * Corporate actions to perform before the transaction date
     */
    List<CorporateActionEntity> findByStockCodeAndTypeInAndRecordDateBefore(String stockCode, List<CorporateActionType> types, LocalDate transactionDate);

    @Query("{'stock_code': ?0, 'type': { $in: ?1 }, 'record_date': { $gte: ?2, $lte: ?3 } }")
    List<CorporateActionEntity> findByStockCodeAndTypeInAndRecordDateBetween(String stockCode, List<CorporateActionType> types, LocalDate start, LocalDate transactionDate);

    @Query(value = "{'type': { $in: ?0 }, 'record_date': { $gte: ?1, $lte: ?2 } }", sort = "{ 'record_date' : -1 }")
    List<CorporateActionEntity> findByTypeInAndRecordDateBetween(List<CorporateActionType> types, LocalDate start, LocalDate transactionDate);

    @Query(value = "{'stock_code': ?0, 'record_date': ?1 }", sort = "{ 'priority' : 1 }")
    List<CorporateActionEntity> findByStockCodeAndRecordDateAndOrderByPriorityAsc(String stockCode, LocalDate recordDate);
}
