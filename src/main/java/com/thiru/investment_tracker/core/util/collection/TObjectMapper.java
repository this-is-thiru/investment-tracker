package com.thiru.investment_tracker.core.util.collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;
import java.util.List;

public class TObjectMapper {

    private static final ObjectMapper OBJECT_MAPPER = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        return readValue(writeValueAsString(source), targetClass);
    }

    public static <T> List<T> readAsList(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            JavaType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
            return OBJECT_MAPPER.readValue(content, listType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    public static <T> List<T> readAsList(Object object, Class<T> targetClass) {

        if (object == null) {
            return null;
        }

        try {
            String content = writeValueAsString(object);
            JavaType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
            return OBJECT_MAPPER.readValue(content, listType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    private static <T> T readValue(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(content, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String writeValueAsString(Object source) {
        try {
            return OBJECT_MAPPER.writeValueAsString(source);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
