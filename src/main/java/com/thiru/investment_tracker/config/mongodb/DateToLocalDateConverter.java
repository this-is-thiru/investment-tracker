package com.thiru.investment_tracker.config.mongodb;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneOffset;

public class DateToLocalDateConverter implements Converter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date source) {
        // Convert a Date object to LocalDate
        return source.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }
}
