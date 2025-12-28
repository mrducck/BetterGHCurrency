package com.gencore.economy.util;

import java.text.DecimalFormat;


public class NumberFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");


    public static double parseFormattedNumber(String input) throws NumberFormatException {
        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Input cannot be null or empty");
        }

        input = input.toLowerCase().trim().replace(",", "");

        double multiplier = 1;
        String numberPart = input;

        // Check for suffix
        if (input.endsWith("k")) {
            multiplier = 1_000;
            numberPart = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 1_000_000;
            numberPart = input.substring(0, input.length() - 1);
        } else if (input.endsWith("b")) {
            multiplier = 1_000_000_000;
            numberPart = input.substring(0, input.length() - 1);
        }

        try {
            double number = Double.parseDouble(numberPart);
            return number * multiplier;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format: " + input);
        }
    }


    public static long parseFormattedLong(String input) throws NumberFormatException {
        return (long) parseFormattedNumber(input);
    }


    public static String formatNumber(double number) {
        return DECIMAL_FORMAT.format(number);
    }


    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }


    public static String formatAbbreviated(double number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.2fK", number / 1_000);
        } else {
            return String.format("%.2f", number);
        }
    }


    public static String formatAbbreviated(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", (double) number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", (double) number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.2fK", (double) number / 1_000);
        } else {
            return String.valueOf(number);
        }
    }
}