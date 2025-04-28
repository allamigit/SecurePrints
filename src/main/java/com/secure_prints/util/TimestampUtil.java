package com.secure_prints.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimestampUtil {

    /**
     * Combine date with time to get timestamp string
     * @param strDate strDate
     * @param strTime strTime
     * @return String
     */
    public static OffsetDateTime getOffsetTimestamp(String strDate, String strTime) {
        String strTimestamp = strDate + " " + strTime + ":00";
        return getOffsetDateTime(strTimestamp);
    }

    /**
     * Convert Timestamp from string to OffsetDateTime format
     * @param strTimestamp strTimestamp
     * @return OffsetDateTime
     */
    public static OffsetDateTime getOffsetDateTime(String strTimestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(strTimestamp, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("America/New_York"));
        return zonedDateTime.toOffsetDateTime();
    }

    /**
     * Validate timestamp to be after current and not in weekend
     * @param timestamp timestamp
     * @return TRE/FALSE
     */
    public static boolean isValidTimestamp(OffsetDateTime timestamp) {
        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        return timestamp.isBefore(currentTimestamp) || timestamp.getDayOfWeek().equals(DayOfWeek.SATURDAY)
               || timestamp.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

}
