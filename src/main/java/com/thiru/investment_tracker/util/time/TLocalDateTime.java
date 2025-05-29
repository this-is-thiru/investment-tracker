package com.thiru.investment_tracker.util.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TLocalDateTime {
    public static final String COMPLETE_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";

    public static LocalDateTime now() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalDateTime atUtc(LocalDateTime dateTime, String timezoneId) {
        return atGivenZone(dateTime, timezoneId, "UTC");
    }

    public static LocalDateTime atUtc(long dateTimestamp, String zoneId) {
        ZonedDateTime fromZonedDateTime = zonedDateTime(dateTimestamp, zoneId);
        ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return toZonedDateTime.toLocalDateTime();
    }

    public static ZonedDateTime zonedDateTime(long timestamp, String zoneId) {
        if (!isInSeconds(timestamp)) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zoneId));
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of(zoneId));
    }

    private static boolean isInSeconds(long timestamp) {
        return String.valueOf(timestamp).length() < 12;
    }

//    public static LocalDateTime atGivenZone(LocalDateTime dateTime, String fromZoneId, String toZoneId) {
//        ZonedDateTime fromZonedDateTime = dateTime.atZone(ZoneId.of(fromZoneId));
//        ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of(toZoneId));
//        return toZonedDateTime.toLocalDateTime();
//    }

    public static LocalDateTime atGivenZone(LocalDateTime dateTime, String fromZoneId, String toZoneId) {
        ZonedDateTime fromZonedDateTime = dateTime.atZone(ZoneId.of(fromZoneId));
        ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of(toZoneId));
        return toZonedDateTime.toLocalDateTime();
    }
}
