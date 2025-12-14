package com.thiru.investment_tracker.util.collection;

import io.micrometer.common.util.StringUtils;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;


public class TJsonMapper {

    private static final JsonMapper JSON_MAPPER = getJsonMapper();

    private static JsonMapper getJsonMapper() {
        return JsonMapper.builder()
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        return readValue(writeValueAsString(source), targetClass);
    }

    public static <T> List<T> readAsList(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        JavaType listType = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
        return JSON_MAPPER.readValue(content, listType);
    }

    public static <T> List<T> readAsList(Object object, Class<T> targetClass) {

        if (object == null) {
            return null;
        }

        String content = writeValueAsString(object);
        JavaType listType = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
        return JSON_MAPPER.readValue(content, listType);
    }

    private static <T> T readValue(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return JSON_MAPPER.readValue(content, targetClass);
    }

    public static String writeValueAsString(Object source) {
        System.out.println(JSON_MAPPER.writeValueAsString(source));
        return JSON_MAPPER.writeValueAsString(source);
    }
}
