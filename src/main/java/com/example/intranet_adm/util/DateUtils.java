package com.example.intranet_adm.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {

    private DateUtils() {
        // Classe utilitária
    }

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ============================================================
    // FORMATAÇÃO
    // ============================================================

    public static String formatDate(LocalDate date) {

        if (date == null) {
            return "";
        }

        return date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {

        if (time == null) {
            return "";
        }

        return time.format(TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    // ============================================================
    // CONVERSÃO
    // ============================================================

    public static LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return LocalDate.parse(
                    value.trim(),
                    DATE_FORMATTER
            );

        } catch (DateTimeParseException error) {

            return null;
        }
    }

    public static LocalTime parseTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return LocalTime.parse(
                    value.trim(),
                    TIME_FORMATTER
            );

        } catch (DateTimeParseException error) {

            return null;
        }
    }

    public static LocalDateTime parseDateTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return LocalDateTime.parse(
                    value.trim(),
                    DATE_TIME_FORMATTER
            );

        } catch (DateTimeParseException error) {

            return null;
        }
    }

    // ============================================================
    // VALIDAÇÃO
    // ============================================================

    public static boolean isValidDate(String value) {

        return parseDate(value) != null;
    }

    public static boolean isValidTime(String value) {

        return parseTime(value) != null;
    }

    public static boolean isValidDateTime(String value) {

        return parseDateTime(value) != null;
    }

    // ============================================================
    // COMPARAÇÃO
    // ============================================================

    public static boolean isBefore(
            LocalDate first,
            LocalDate second
    ) {

        if (first == null || second == null) {
            return false;
        }

        return first.isBefore(second);
    }

    public static boolean isAfter(
            LocalDate first,
            LocalDate second
    ) {

        if (first == null || second == null) {
            return false;
        }

        return first.isAfter(second);
    }

    public static boolean isToday(LocalDate date) {

        return date != null
                && date.equals(LocalDate.now());
    }

    public static boolean isPast(LocalDate date) {

        return date != null
                && date.isBefore(LocalDate.now());
    }

    public static boolean isFuture(LocalDate date) {

        return date != null
                && date.isAfter(LocalDate.now());
    }

    // ============================================================
    // DATA ATUAL
    // ============================================================

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalTime now() {
        return LocalTime.now();
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
}