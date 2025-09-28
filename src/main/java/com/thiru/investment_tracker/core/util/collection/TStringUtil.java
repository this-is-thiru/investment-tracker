package com.thiru.investment_tracker.core.util.collection;

import java.util.ArrayList;
import java.util.List;

public class TStringUtil {
    public static List<String> splitSafeTrimmed(String string, String delimiter) {
        return splitSafeTrimmed(string, delimiter, -1);
    }

    public static List<String> splitSafeTrimmed(String string, String delimiter, int limit) {
        if (string == null) {
            string = "";
        }

        String[] strings = string.split(delimiter, limit);
        int length = strings.length;

        List<String> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String str = strings[i];
            str = str.trim();
            if (!str.isEmpty()) {
                list.add(str);
            }
        }
        return list;
    }
}
