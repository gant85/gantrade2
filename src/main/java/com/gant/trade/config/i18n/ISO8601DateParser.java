package com.gant.trade.config.i18n;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ISO8601DateParser {
    private static final String PARSE_EXCEPTION_ISO8601_DATE_FORMAT_WRONG = "Parse exception ISO8601, date format wrong";
    private static final String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static String defaultTimeZoneFormatPattern = "Z";
    private static String defaultTimestampFormatPattern = "yyyy-MM-dd HH:mm:ss";
    private static final String NO_TZ_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final int TIMEZONE_PATTERN_LENGTH = 5;

    private ISO8601DateParser() {
    }

    public static Date fromISODateString(String isoDateString) throws ParseException {
        DateFormat f = new SimpleDateFormat(FORMAT_DATE_ISO);
        String stringToParse = preProcessIsoDateString(isoDateString);
        return f.parse(stringToParse);
    }

    private static String preProcessIsoDateString(String isoDateString) throws ParseException {
        String result = null;
        SimpleDateFormat timeZoneFormatter = new SimpleDateFormat(defaultTimeZoneFormatPattern);
        SimpleDateFormat formatter = new SimpleDateFormat(defaultTimestampFormatPattern);
        Date d;
        switch (isoDateString.length()) {
            case 4:
                d = formatter.parse(isoDateString + "-01-01 00:00:00");
                result = isoDateString + "-01-01T00:00:00" + timeZoneFormatter.format(d);
                break;
            case 7:
                d = formatter.parse(isoDateString + "-01 00:00:00");
                result = isoDateString + "-01T00:00:00" + timeZoneFormatter.format(d);
                break;
            case 10:
                d = formatter.parse(isoDateString + " 00:00:00");
                result = isoDateString + "T00:00:00" + timeZoneFormatter.format(d);
                break;
            case 21:
                String[] date = splitDate(isoDateString);
                result = date[0] + ":00+" + date[1];
                break;
            default:
                result = isoDateString;
        }

        return result;
    }

    private static String[] splitDate(String isoDateString) {
        return isoDateString.split("\\+");
    }

    public static String toISOString(Date date, String format, TimeZone tz) {
        if (format == null) {
            format = FORMAT_DATE_ISO;
        }

        if (tz == null) {
            tz = TimeZone.getDefault();
        }

        DateFormat f = new SimpleDateFormat(format);
        f.setTimeZone(tz);
        return f.format(date);
    }

    public static String toISOString(Date date) {
        return toISOString(date, FORMAT_DATE_ISO, TimeZone.getDefault());
    }

    public static String format(Date date) {
        return format(date, TimeZone.getDefault(), Locale.getDefault());
    }

    public static String format(Date date, TimeZone timeZone) {
        return format(date, timeZone, Locale.getDefault());
    }

    public static String format(Date date, TimeZone timeZone, Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(NO_TZ_FORMAT_ISO, locale);
        DateFormat timeZoneFormat = new SimpleDateFormat("Z", locale);
        timeZoneFormat.setTimeZone(timeZone);
        StringBuilder isoFormat = new StringBuilder(28);
        isoFormat.append(dateFormat.format(date));
        isoFormat.append(timeZoneFormat.format(date));
        return isoFormat.toString();
    }

    public static Calendar parse(String date) {
        return parse(date, Locale.getDefault());
    }

    public static Calendar parse(String date, Locale locale) {
        return parseDateToCalendar(date, locale);
    }

    private static Calendar parseDateToCalendar(String date, Locale locale) {
        if (date.length() <= 28 && date.length() >= 4) {
            Calendar calendar = new GregorianCalendar();
            DateFormat noTZFormatter = new SimpleDateFormat(NO_TZ_FORMAT_ISO, locale);
            StringBuilder dateISO = new StringBuilder(28);
            Date d = null;

            try {
                switch (date.length()) {
                    case 4:
                        dateISO.append(date);
                        dateISO.append("-01-01T00:00:00.000");
                        d = noTZFormatter.parse(dateISO.toString());
                        break;
                    case 7:
                        dateISO.append(date);
                        dateISO.append("-01T00:00:00.000");
                        d = noTZFormatter.parse(dateISO.toString());
                        break;
                    case 10:
                        dateISO.append(date);
                        dateISO.append("T00:00:00.000");
                        d = noTZFormatter.parse(dateISO.toString());
                        break;
                    case 18:
                        dateISO.append(date);
                        dateISO.insert(13, ":00:00.000");
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 21:
                        dateISO.append(date);
                        dateISO.insert(16, ":00.000");
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 24:
                        dateISO.append(date);
                        dateISO.insert(19, ".000");
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 26:
                        dateISO.append(date);
                        dateISO.insert(21, "00");
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 27:
                        dateISO.append(date);
                        dateISO.insert(22, "0");
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 28:
                        dateISO.append(date);
                        d = noTZFormatter.parse(dateISO.toString());
                        calendar.setTimeZone(parseTimeZone(date));
                        break;
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 19:
                    case 20:
                    case 22:
                    case 23:
                    case 25:
                    default:
                        throw new IllegalArgumentException(PARSE_EXCEPTION_ISO8601_DATE_FORMAT_WRONG);
                }
            } catch (ParseException var7) {
                throw new IllegalArgumentException(PARSE_EXCEPTION_ISO8601_DATE_FORMAT_WRONG, var7);
            }

            if (d != null) {
                calendar.setTime(d);
            }

            return calendar;
        } else {
            throw new IllegalArgumentException(PARSE_EXCEPTION_ISO8601_DATE_FORMAT_WRONG);
        }
    }

    private static TimeZone parseTimeZone(String date) {
        StringBuilder timeZoneID = new StringBuilder(8);
        timeZoneID.append(date.substring(date.length() - TIMEZONE_PATTERN_LENGTH, date.length()));
        SimpleDateFormat timeZoneFormatter = new SimpleDateFormat(defaultTimeZoneFormatPattern);

        try {
            timeZoneFormatter.parse(timeZoneID.toString());
        } catch (ParseException var4) {
            throw new IllegalArgumentException("Parse exception in parse time zone value", var4);
        }

        timeZoneID.insert(0, "GMT");
        return TimeZone.getTimeZone(timeZoneID.toString());
    }
}

