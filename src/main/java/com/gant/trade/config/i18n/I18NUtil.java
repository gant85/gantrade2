package com.gant.trade.config.i18n;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public interface I18NUtil {
    int compare(String var1, String var2);

    int compare(String var1, String var2, Locale var3);

    String getMessage(String var1);

    String getMessage(String var1, Locale var2);

    Calendar convertToGMTForCalendar(Date var1);

    Calendar convertToGMTForCalendar(Date var1, TimeZone var2);

    Date convertToGMTForDate(Date var1);

    Date convertToGMTForDate(Date var1, TimeZone var2);

    Calendar convertToLocalTZForCalendar(Date var1);

    Calendar convertToLocalTZForCalendar(Date var1, TimeZone var2);

    Date convertToLocalTZForDate(Date var1);

    Date convertToLocalTZForDate(Date var1, TimeZone var2);

    String formatCompoundMessage(String var1, Object[] var2);

    String formatCompoundMessage(String var1, Object[] var2, Locale var3);

    String formatCompoundMessageChoiceFormat(String var1, Format[] var2, Object[] var3);

    String formatCompoundMessageChoiceFormat(String var1, Format[] var2, Object[] var3, Locale var4);

    String formatDate(Date var1);

    String formatDate(Date var1, int var2);

    String formatDate(Date var1, int var2, Locale var3);

    String formatDate(Date var1, String var2);

    String formatDateTime(Date var1);

    String formatDateTime(Date var1, int var2);

    String formatDateTime(Date var1, int var2, Locale var3);

    String formatDateTime(Date var1, String var2);

    String formatISO8601(Date var1, TimeZone var2);

    String formatNumber(Long var1, String var2);

    String formatNumber(Long var1, String var2, Locale var3);

    String formatNumber(Long var1);

    String formatNumber(Long var1, Locale var2);

    String formatNumber(Double var1, String var2);

    String formatNumber(Double var1, String var2, Locale var3);

    String formatNumber(Double var1);

    String formatNumber(Double var1, Locale var2);

    String formatPercent(Double var1);

    String formatPercent(Double var1, Locale var2);

    String formatTime(Date var1);

    String formatTime(Date var1, int var2);

    String formatTime(Date var1, int var2, Locale var3);

    String formatTime(Date var1, String var2);

    Date parseDate(String var1);

    Date parseDate(String var1, int var2);

    Date parseDate(String var1, int var2, Locale var3);

    Date parseDate(String var1, String var2);

    Date parseDateTime(String var1);

    Date parseDateTime(String var1, int var2);

    Date parseDateTime(String var1, int var2, Locale var3);

    Date parseDateTime(String var1, String var2);

    Date parseISO8601(String var1);

    Double parseNumberForDouble(String var1);

    Double parseNumberForDouble(String var1, Locale var2);

    Long parseNumberForLong(String var1);

    Long parseNumberForLong(String var1, Locale var2);

    Double parsePercentForDouble(String var1);

    Double parsePercentForDouble(String var1, Locale var2);

    Date parseTime(String var1);

    Date parseTime(String var1, int var2);

    Date parseTime(String var1, int var2, Locale var3);

    Date parseTime(String var1, String var2);

    TimeZone getTimeZone();

    Locale getLocale();
}


