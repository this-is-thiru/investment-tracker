package com.thiru.investment_tracker.repository.test;

import com.thiru.investment_tracker.entity.test.EntityTest1;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;

@JaversSpringDataAuditable
public interface EntityTest1Repository extends MongoRepository<EntityTest1, String> {

}
