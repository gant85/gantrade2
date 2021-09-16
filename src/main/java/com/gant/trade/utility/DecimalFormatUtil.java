package com.gant.trade.utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalFormatUtil {

    private DecimalFormatUtil(){}

    private static final int DECIMAL = 6;

    public static String format(Double value) {
        return format(DECIMAL, value, Locale.ENGLISH);
    }

    public static String format(int decimal, Double value, Locale locale) {
        return new DecimalFormat(pattern(decimal), new DecimalFormatSymbols(locale)).format(value);
    }

    private static String pattern(int decimal) {
        String pattern = "#.";
        for (int i = 0; i < decimal; i++) {
            pattern = pattern.concat("#");
        }
        return pattern;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
