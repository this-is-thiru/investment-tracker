package com.thiru.investment_tracker.util.collection;

import com.thiru.investment_tracker.dto.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void readObject() {
        Student student = new Student("Thiru", LocalDate.now(), LocalDateTime.now());
        Student student1 = TObjectMapper.copy(student, Student.class);
        Assertions.assertEquals(student1.name(), student.name());
        Assertions.assertEquals(student1.dob(), student.dob());
        Assertions.assertEquals(student1.createdDate(), student.createdDate());
    }
}