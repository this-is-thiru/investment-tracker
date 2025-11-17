package com.thiru.investment_tracker.util.collection;

import io.micrometer.common.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.CollectionType;

import java.util.List;

public class TObjectMapper {

    private static final JsonMapper OBJECT_MAPPER = getObjectMapper();

    private static JsonMapper getObjectMapper() {
        return new JsonMapper();
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        return readValue(writeValueAsString(source), targetClass);
    }

    public static <T> List<T> readAsList(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
        return OBJECT_MAPPER.readValue(content, listType);
    }

    public static <T> List<T> readAsList(Object object, Class<T> targetClass) {

        if (object == null) {
            return null;
        }

        String content = writeValueAsString(object);
        CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass);
        return OBJECT_MAPPER.readValue(content, listType);
    }

    private static <T> T readValue(String content, Class<T> targetClass) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return OBJECT_MAPPER.readValue(content, targetClass);
    }

    public static String writeValueAsString(Object source) {
        return OBJECT_MAPPER.writeValueAsString(source);
    }
}
