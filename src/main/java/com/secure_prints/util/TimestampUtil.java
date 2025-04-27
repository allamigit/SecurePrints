package com.secure_prints.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampUtil {

    /**
     * Combine date with time to get timestamp string
     * @param strDate strDate
     * @param strTime strTime
     * @return String
     */
    public static  String getTimestamp(String strDate, String strTime) {
        return strDate + " " + strTime;
    }

    /**
     * Convert Timestamp from string to OffsetDateTime format
     * @param strTimestamp strTimestamp
     * @return OffsetDateTime
     */
    public static  OffsetDateTime getOffsetDateTime(String strTimestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(strTimestamp, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("America/New_York"));
        return zonedDateTime.toOffsetDateTime();
    }

}
