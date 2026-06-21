package com.thiru.wealthlens.repository;

import com.thiru.wealthlens.entity.TradeOutcomeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOutcomeRepository extends MongoRepository<TradeOutcomeEntity, String> {

    List<TradeOutcomeEntity> findByEmail(String email);

    void deleteByEmail(String email);
}
