package com.secure.prints.util;

import com.secure.prints.model.DateRange;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimestampUtil {

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
     * Validate timestamp to be after current and not in weekend
     * @param timestamp timestamp
     * @return TRUE/FALSE
     */
    public static boolean isValidTimestamp(OffsetDateTime timestamp) {
        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        return timestamp.isBefore(currentTimestamp) || timestamp.getDayOfWeek() == DayOfWeek.SATURDAY
               || timestamp.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Format local date as 'MMM dd, yyyy'
     * @param date date
     * @return Formatted local date
     */
    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
        return date.format(formatter);
    }

    /**
     * Format offset timestamp as 'EEEE, MMM dd, yyyy @ h:mm a'
     * @param timestamp timestamp
     * @return Formatted offset timestamp
     */
    public static String formatDateTime(OffsetDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy @ h:mm a");
        return timestamp.format(formatter);
    }

    /**
     * Format offset timestamp as 'MM/dd/yyyy - h:mm a'
     * @param timestamp timestamp
     * @return Formatted offset timestamp
     */
    public static String formatTimestamp(OffsetDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - h:mm a");
        return timestamp.format(formatter);
    }

}
