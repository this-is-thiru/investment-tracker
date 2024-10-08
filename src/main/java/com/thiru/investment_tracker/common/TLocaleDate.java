package com.thiru.investment_tracker.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TLocaleDate {

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TCommonUtil.DATE_FORMAT);
        return LocalDate.parse(date, formatter);
    }
}
