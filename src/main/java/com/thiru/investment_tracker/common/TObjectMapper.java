package com.thiru.investment_tracker.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;

public class TObjectMapper {

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
}
