package com.thiru.investment_tracker.common.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.thiru.investment_tracker.common.TCommonUtil;

public class ParserUtil {

    public static LocalDate convertToDate(String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TCommonUtil.DATE_FORMAT);
        return LocalDate.parse(date, formatter);
    }
}
