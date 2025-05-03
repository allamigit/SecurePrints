package com.secure.prints.util;

import com.secure.prints.model.DateRange;

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
        String strTimestamp = strDate + " " + strTime;
        return getOffsetDateTime(strTimestamp);
    }

    /**
     * Convert LocalDate range to OffsetDateTime range for given start and end dates
     * @param startDate startDate
     * @param endDate endDate
     * @return DateRange
     */
    public static DateRange getOffsetDateRange(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startTimestamp = getOffsetDateTime(startDate + " 00:00:00");
        OffsetDateTime endTimestamp = getOffsetDateTime(endDate + " 23:59:59");
        return DateRange.builder()
                .startTimestamp(startTimestamp)
                .endTimestamp(endTimestamp)
                .build();
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
        return timestamp.isBefore(currentTimestamp) || timestamp.getDayOfWeek() == DayOfWeek.SATURDAY
               || timestamp.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

}
