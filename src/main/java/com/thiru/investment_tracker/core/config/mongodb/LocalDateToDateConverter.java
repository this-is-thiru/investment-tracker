package com.thiru.investment_tracker.core.config.mongodb;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneOffset;

public class LocalDateToDateConverter implements Converter<LocalDate, Date> {

    @Override
    public Date convert(LocalDate source) {
        // Convert LocalDate to a Date object at midnight UTC
        return Date.from(source.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
