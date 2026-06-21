package com.thiru.wealthlens.repository;

import com.thiru.wealthlens.dto.enums.BrokerName;
import com.thiru.wealthlens.entity.BrokerCharges;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface BrokerChargesRepository extends MongoRepository<BrokerCharges, String> {

    @Query("{'broker_name': ?0, 'status': 'ACTIVE', 'start_date': { $lte: ?1 }, 'end_date': { $gte: ?1 } }")
    Optional<BrokerCharges> findActiveBrokerChargesOnDate(BrokerName brokerName, LocalDate targetDate);

}
