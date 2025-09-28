package com.thiru.investment_tracker.util.collection;

import com.thiru.investment_tracker.core.util.collection.TObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TObjectMapperTest {

    @Test
    void readAsList() {
        // Test String List
        List<String> stringList = List.of("string", "string1", "string2");
        String content = TObjectMapper.writeValueAsString(stringList);
        List<String> list = TObjectMapper.readAsList(content, String.class);

        Assertions.assertEquals(stringList, list);

        // Test Integer List
        List<Integer> integerList = List.of(1, 2, 3);
        content = TObjectMapper.writeValueAsString(integerList);
        List<Integer> integers = TObjectMapper.readAsList(content, Integer.class);

        Assertions.assertEquals(integerList, integers);
    }
}