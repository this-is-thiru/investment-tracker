package com.thiru.investment_tracker.util.collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;
import java.util.List;

public class TObjectMapper {

    private static final ObjectMapper OBJECT_MAPPER = getObjectMapper();
    private static final ObjectMapper OBJECT_MAPPER_SAFE = safeObjectMapper();

    private static ObjectMapper safeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        return readValue(OBJECT_MAPPER, writeValueAsString(OBJECT_MAPPER, source), targetClass);
    }

    public static <T> T safeCopy(Object source, Class<T> targetClass) {
        return readValue(OBJECT_MAPPER_SAFE, writeValueAsString(OBJECT_MAPPER_SAFE, source), targetClass);
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

    public static <T> List<T> safeReadAsList(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            JavaType listType = OBJECT_MAPPER_SAFE.getTypeFactory().constructCollectionType(List.class, targetClass);
            return OBJECT_MAPPER_SAFE.readValue(content, listType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    public static <T> List<T> readAsList(Object object, Class<T> targetClass) {

        if (object == null) {
            return null;
        }

        try {
            String content = writeValueAsString(OBJECT_MAPPER, object);
            JavaType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
            return OBJECT_MAPPER.readValue(content, listType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    public static <T> List<T> safeReadAsList(Object object, Class<T> targetClass) {

        if (object == null) {
            return null;
        }

        try {
            String content = writeValueAsString(OBJECT_MAPPER_SAFE, object);
            JavaType listType = OBJECT_MAPPER_SAFE.getTypeFactory().constructCollectionType(List.class, targetClass);
            return OBJECT_MAPPER_SAFE.readValue(content, listType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    private static <T> T readValue(ObjectMapper objectMapper, String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            return objectMapper.readValue(content, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String writeValueAsString(ObjectMapper objectMapper, Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
