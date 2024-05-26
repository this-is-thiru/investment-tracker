package com.thiru.investment_tracker.common;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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