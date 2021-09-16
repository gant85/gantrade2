package com.gant.trade.config.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.text.*;
import java.util.*;

public abstract class AbstractI18NUtil implements I18NUtil {

    private static final String UNACCEPTABLE_STRING_INPUT_PATTERN = "Unacceptable string input pattern ";
    private static final String UNABLE_TO_PARSE_INPUT_JAVA_UTIL_DATE_FOR_THIS_FORMAT_PATTERN = "Unable to parse input java.util.Date for this format pattern ";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractI18NUtil.class);
    @Autowired
    private MessageSource messageSource;

    protected AbstractI18NUtil() {
    }

    public int compare(String source, String target) {
        Locale locale = this.getLocale();
        return this.compare(source, target, locale);
    }

    public int compare(String source, String target, Locale locale) {
        Collator collator = Collator.getInstance(locale);
        if (source != null && target != null) {
            return collator.compare(source, target);
        } else {
            throw new IllegalArgumentException("Compare null argument");
        }
    }

    public Calendar convertToGMTForCalendar(Date date) {
        TimeZone timeZone = this.getTimeZone();
        return this.convertToGMTForCalendar(date, timeZone);
    }

    public Calendar convertToGMTForCalendar(Date date, TimeZone timeZone) {
        Locale locale = this.getLocale();
        if (date != null && timeZone != null) {
            int offset = timeZone.getOffset(date.getTime());
            long dateValue = date.getTime();
            Date updatedDate = new Date(dateValue - (long) offset);
            Calendar calendar = new GregorianCalendar(timeZone, locale);
            calendar.setTime(updatedDate);
            return calendar;
        } else {
            throw new IllegalArgumentException("convertToGMTForCalendar null argument");
        }
    }

    public Date convertToGMTForDate(Date date) {
        TimeZone timeZone = this.getTimeZone();
        return this.convertToGMTForDate(date, timeZone);
    }

    public Date convertToGMTForDate(Date date, TimeZone timeZone) {
        if (date != null && timeZone != null) {
            int offset = timeZone.getOffset(date.getTime());
            long dateValue = date.getTime();
            return new Date(dateValue - (long) offset);
        } else {
            throw new IllegalArgumentException("convertToGMTForDate null argument");
        }
    }

    public Calendar convertToLocalTZForCalendar(Date date) {
        TimeZone timeZone = this.getTimeZone();
        return this.convertToLocalTZForCalendar(date, timeZone);
    }

    public Calendar convertToLocalTZForCalendar(Date date, TimeZone timeZone) {
        if (date != null && timeZone != null) {
            Locale locale = this.getLocale();
            int offset = timeZone.getOffset(date.getTime());
            long dateValue = date.getTime();
            Date updatedDate = new Date(dateValue + (long) offset);
            Calendar calendar = new GregorianCalendar(timeZone, locale);
            calendar.setTime(updatedDate);
            return calendar;
        } else {
            throw new IllegalArgumentException("convertToLocalTZForCalendar null argument");
        }
    }

    public Date convertToLocalTZForDate(Date date) {
        TimeZone timeZone = this.getTimeZone();
        return this.convertToLocalTZForDate(date, timeZone);
    }

    public Date convertToLocalTZForDate(Date date, TimeZone timeZone) {
        if (date != null && timeZone != null) {
            int offset = timeZone.getOffset(date.getTime());
            long dateValue = date.getTime();
            return new Date(dateValue + (long) offset);
        } else {
            throw new IllegalArgumentException("convertToLocalTZForCalendar null argument");
        }
    }

    public String formatCompoundMessage(String messageKey, Object[] messageArguments) {
        Locale locale = this.getLocale();
        return this.formatCompoundMessage(messageKey, messageArguments, locale);
    }

    public String formatCompoundMessage(String messageKey, Object[] messageArguments, Locale locale) {
        if (messageKey != null && locale != null) {
            MessageFormat formatter = new MessageFormat("");
            formatter.setLocale(locale);
            formatter.applyPattern(this.getMessage(messageKey, messageArguments, locale));
            return formatter.format(messageArguments);
        } else {
            throw new IllegalArgumentException("formatCompoundMessage null argument");
        }
    }

    public String formatCompoundMessageChoiceFormat(String messageKey, Format[] newFormats, Object[] messageArguments) {
        Locale locale = this.getLocale();
        return this.formatCompoundMessageChoiceFormat(messageKey, newFormats, messageArguments, locale);
    }

    public String formatCompoundMessageChoiceFormat(String messageKey, Format[] newFormats, Object[] messageArguments, Locale locale) {
        if (messageKey != null && locale != null && newFormats != null) {
            MessageFormat messageFormat = new MessageFormat("");
            messageFormat.setLocale(locale);
            messageFormat.applyPattern(this.getMessage(messageKey, (Object[]) null, locale));
            messageFormat.setFormats(newFormats);
            return messageFormat.format(messageArguments);
        } else {
            throw new IllegalArgumentException("formatCompoundMessage null argument");
        }
    }

    public String formatDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("formatDate null argument");
        } else {
            return this.formatDate(date, 2);
        }
    }

    public String formatDate(Date date, int style) {
        Locale locale = this.getLocale();
        return this.formatDate(date, style, locale);
    }

    public String formatDate(Date date, String pattern) {
        if (date != null && pattern != null) {
            if (!pattern.isEmpty() && !pattern.contains("H") && !pattern.contains("m") && !pattern.contains("s")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                return dateFormat.format(date);
            } else {
                throw new IllegalArgumentException(UNACCEPTABLE_STRING_INPUT_PATTERN + pattern);
            }
        } else {
            throw new IllegalArgumentException("formatDate(Date date, String format) null argument");
        }
    }

    public String formatDateTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("formatDateTime null argument");
        } else {
            return this.formatDateTime(date, 2);
        }
    }

    public String formatDateTime(Date date, int style) {
        Locale locale = this.getLocale();
        return this.formatDateTime(date, style, locale);
    }

    public String formatDateTime(Date date, String pattern) {
        if (date != null && pattern != null && !pattern.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(date);
        } else {
            throw new IllegalArgumentException("formatDateTime(Date date, String pattern) null argument");
        }
    }

    public String formatISO8601(Date date, TimeZone timeZone) {
        if (date != null && timeZone != null) {
            Locale locale = this.getLocale();
            return ISO8601DateParser.format(date, timeZone, locale);
        } else {
            throw new IllegalArgumentException("formatISO8601 null argument");
        }
    }

    public String formatNumber(Long number, String pattern) {
        Locale locale = this.getLocale();
        return this.formatNumber(number, pattern, locale);
    }

    public String formatNumber(Long number, String pattern, Locale locale) {
        if (number != null && pattern != null && locale != null) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
            decimalFormat.applyPattern(pattern);
            return decimalFormat.format(number);
        } else {
            throw new IllegalArgumentException("formatNumber null argument");
        }
    }

    public String formatNumber(Long number) {
        Locale locale = this.getLocale();
        return this.formatNumber(number, locale);
    }

    public String formatNumber(Double number, String pattern) {
        Locale locale = this.getLocale();
        return this.formatNumber(number, pattern, locale);
    }

    public String formatNumber(Double number, String pattern, Locale locale) {
        if (number != null && pattern != null && locale != null) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
            decimalFormat.applyPattern(pattern);
            return decimalFormat.format(number);
        } else {
            throw new IllegalArgumentException("formatNumber null argument");
        }
    }

    public String formatNumber(Double number) {
        Locale locale = this.getLocale();
        return this.formatNumber(number, locale);
    }

    public String formatPercent(Double number) {
        Locale locale = this.getLocale();
        return this.formatPercent(number, locale);
    }

    public String formatTime(Date time) {
        if (time == null) {
            throw new IllegalArgumentException("formatDateTime null argument");
        } else {
            return this.formatTime(time, 2);
        }
    }

    public String formatTime(Date date, int style) {
        Locale locale = this.getLocale();
        return this.formatTime(date, style, locale);
    }

    public String formatTime(Date date, String pattern) {
        if (date != null && pattern != null) {
            if (!pattern.isEmpty() && !pattern.contains("y") && !pattern.contains("M") && !pattern.contains("d")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                return dateFormat.format(date);
            } else {
                throw new IllegalArgumentException(UNACCEPTABLE_STRING_INPUT_PATTERN + pattern);
            }
        } else {
            throw new IllegalArgumentException("formatTime(Date date, String pattern) null argument");
        }
    }

    public Date parseDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("parseDate null argument");
        } else {
            return this.parseDate(date, 2);
        }
    }

    public Date parseDate(String date, int style) {
        Locale locale = this.getLocale();
        return this.parseDate(date, style, locale);
    }

    public Date parseDate(String date, String pattern) {
        if (date != null && pattern != null) {
            if (!pattern.isEmpty() && !pattern.contains("H") && !pattern.contains("m") && !pattern.contains("s")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

                try {
                    return dateFormat.parse(date);
                } catch (ParseException var5) {
                    throw new IllegalArgumentException(UNABLE_TO_PARSE_INPUT_JAVA_UTIL_DATE_FOR_THIS_FORMAT_PATTERN + pattern, var5);
                }
            } else {
                throw new IllegalArgumentException(UNACCEPTABLE_STRING_INPUT_PATTERN + pattern);
            }
        } else {
            throw new IllegalArgumentException("parseDate(String date, String pattern) null argument");
        }
    }

    public Date parseDateTime(String date) {
        if (date == null) {
            throw new IllegalArgumentException("parseDateTime null argument");
        } else {
            return this.parseDateTime(date, 2);
        }
    }

    public Date parseDateTime(String date, int style) {
        Locale locale = this.getLocale();
        return this.parseDateTime(date, style, locale);
    }

    public Date parseDateTime(String date, String pattern) {
        if (date != null && pattern != null && !pattern.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

            try {
                return dateFormat.parse(date);
            } catch (ParseException var5) {
                throw new IllegalArgumentException(UNABLE_TO_PARSE_INPUT_JAVA_UTIL_DATE_FOR_THIS_FORMAT_PATTERN + pattern, var5);
            }
        } else {
            throw new IllegalArgumentException("parseDateTime(String date, String pattern) null argument");
        }
    }

    public Date parseISO8601(String date) {
        Locale locale = this.getLocale();
        return ISO8601DateParser.parse(date, locale).getTime();
    }

    public Double parseNumberForDouble(String number) {
        Locale locale = this.getLocale();
        return this.parseNumberForDouble(number, locale);
    }

    public Long parseNumberForLong(String number) {
        Locale locale = this.getLocale();
        return this.parseNumberForLong(number, locale);
    }

    public Double parsePercentForDouble(String number) {
        Locale locale = this.getLocale();
        return this.parsePercentForDouble(number, locale);
    }

    public Date parseTime(String time) {
        if (time == null) {
            throw new IllegalArgumentException("parseTime null argument");
        } else {
            return this.parseTime(time, 2);
        }
    }

    public Date parseTime(String date, int style) {
        Locale locale = this.getLocale();
        return this.parseTime(date, style, locale);
    }

    public Date parseTime(String date, String pattern) {
        if (date != null && pattern != null) {
            if (!pattern.isEmpty() && !pattern.contains("y") && !pattern.contains("M") && !pattern.contains("d")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

                try {
                    return dateFormat.parse(date);
                } catch (ParseException var5) {
                    throw new IllegalArgumentException(UNABLE_TO_PARSE_INPUT_JAVA_UTIL_DATE_FOR_THIS_FORMAT_PATTERN + pattern, var5);
                }
            } else {
                throw new IllegalArgumentException("Unacceptable input string pattern: " + pattern);
            }
        } else {
            throw new IllegalArgumentException("parseTime(String date, String pattern) null argument");
        }
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public Locale getLocale() {
        return Locale.getDefault();
    }

    public String getMessage(String code) {
        return this.getMessage(code, this.getLocale());
    }

    public String getMessage(String code, Locale locale) {
        return this.getMessage(code, (Object[]) null, locale);
    }

    private String getMessage(String code, Object[] args, Locale locale) {
        String i18nMessage = code;

        try {
            i18nMessage = this.messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException var6) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No message Found: ", var6);
            }
        }

        return i18nMessage;
    }
}


