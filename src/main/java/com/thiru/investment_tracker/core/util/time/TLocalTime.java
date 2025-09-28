package com.thiru.investment_tracker.core.util.time;

import io.micrometer.common.lang.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TLocalTime {

    public static final String DEFAULT_TIME_FORMAT = "hh:mm a";

    public static LocalTime toLocalTime(@NonNull String time, @NonNull String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(time, formatter);
    }

    public static String format(@NonNull LocalTime time, @NonNull String pattern) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
        return time.format(formatter);
    }
}
