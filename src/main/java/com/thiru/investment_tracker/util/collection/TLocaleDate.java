package com.thiru.investment_tracker.util.collection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TLocaleDate {

    public static String TIME_ZONE_IST = "Asia/Kolkata";

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static String lastYearSameDateInString() {
        return LocalDate.now().minusYears(1).toString();
    }

    public static String convertToString(LocalDate date) {
        return date.toString();
    }

    public static LocalDate convertToDate(String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TCollectionUtil.DATE_FORMAT);
        return LocalDate.parse(date, formatter);
    }

    public static LocalDateTime convertToDateTime(String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TCollectionUtil.DATE_TIME_FORMAT);
        return LocalDateTime.parse(date, formatter);
    }

    public static String standardDateFormattedString(LocalDate date) {

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TCollectionUtil.DATE_FORMAT);
        return localDate.format(formatter);
    }
}
