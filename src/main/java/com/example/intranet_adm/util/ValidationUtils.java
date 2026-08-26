package com.example.intranet_adm.util;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private ValidationUtils() {
        // Classe utilitária
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private static final Pattern TIME_PATTERN =
            Pattern.compile(
                    "^([01]\\d|2[0-3]):[0-5]\\d$"
            );

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "^(https?://).+",
                    Pattern.CASE_INSENSITIVE
            );

    // ============================================================
    // TEXTO
    // ============================================================

    public static boolean isEmpty(String value) {

        return value == null
                || value.isBlank();
    }

    public static boolean isNotEmpty(String value) {

        return !isEmpty(value);
    }

    public static String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // ============================================================
    // TAMANHO
    // ============================================================

    public static boolean hasMinLength(
            String value,
            int minimum
    ) {

        if (value == null) {
            return false;
        }

        return value.trim().length() >= minimum;
    }

    public static boolean hasMaxLength(
            String value,
            int maximum
    ) {

        if (value == null) {
            return true;
        }

        return value.trim().length() <= maximum;
    }

    // ============================================================
    // E-MAIL
    // ============================================================

    public static boolean isValidEmail(String email) {

        if (isEmpty(email)) {
            return false;
        }

        return EMAIL_PATTERN.matcher(
                email.trim()
        ).matches();
    }

    // ============================================================
    // HORÁRIO
    // ============================================================

    public static boolean isValidTime(String time) {

        if (isEmpty(time)) {
            return false;
        }

        return TIME_PATTERN.matcher(
                time.trim()
        ).matches();
    }

    // ============================================================
    // URL
    // ============================================================

    public static boolean isValidUrl(String url) {

        if (isEmpty(url)) {
            return false;
        }

        return URL_PATTERN.matcher(
                url.trim()
        ).matches();
    }

    // ============================================================
    // AVISO
    // ============================================================

    public static boolean isValidTitulo(
            String titulo
    ) {

        return hasMinLength(titulo, 3)
                && hasMaxLength(titulo, 150);
    }

    public static boolean isValidMensagem(
            String mensagem
    ) {

        return hasMinLength(mensagem, 1)
                && hasMaxLength(mensagem, 5000);
    }

    public static boolean isValidAutor(
            String autor
    ) {

        return hasMinLength(autor, 2)
                && hasMaxLength(autor, 100);
    }

    // ============================================================
    // NÚMEROS
    // ============================================================

    public static boolean isPositive(
            int value
    ) {

        return value > 0;
    }

    public static boolean isNonNegative(
            int value
    ) {

        return value >= 0;
    }
}