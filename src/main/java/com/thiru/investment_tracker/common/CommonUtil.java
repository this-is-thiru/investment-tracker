package com.thiru.investment_tracker.common;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommonUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final ObjectMapper objectMapper = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        return readValue(writeValueAsString(source), targetClass);
    }

    private static <T> T readValue(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            return objectMapper.readValue(content, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private static String writeValueAsString(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Collections
    public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
        return stream(source).map(mapper).toList();
    }

    public static <T> List<T> filter(Collection<T> source, Predicate<T> consumer) {
        return stream(source).filter(consumer).toList();
    }

    public static <T, R> void mapAndApply(Collection<T> source, Function<T, R> mapper, Consumer<R> consumer) {
        stream(source).map(mapper).forEach(consumer);
    }

    public static <T> List<T> flatMap(Collection<List<T>> source) {
        return stream(source).flatMap(CommonUtil::stream).toList();
    }

    public static <T> LongStream mapToLong(List<T> list, ToLongFunction<T> mapper) {
        return stream(list).mapToLong(mapper);
    }

    private static <T> Stream<T> stream(Collection<T> list) {
        return list.stream();
    }
}