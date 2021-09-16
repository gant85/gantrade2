package com.gant.trade.config.i18n;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class I18NUtilImpl extends AbstractI18NUtil {
    public I18NUtilImpl() {
        //I18NAutoConfiguration
    }

    public String formatDate(Date date, int style, Locale locale) {
        if (date != null && locale != null) {
            if (!this.isStyleDateFormatValid(style)) {
                throw new IllegalArgumentException("formatDate illegal style argument");
            } else {
                DateFormat dateFormat = DateFormat.getDateInstance(style, locale);
                return dateFormat.format(date);
            }
        } else {
            throw new IllegalArgumentException("formatDate null argument");
        }
    }

    public String formatDateTime(Date date, int style, Locale locale) {
        if (date != null && locale != null) {
            if (!this.isStyleDateFormatValid(style)) {
                throw new IllegalArgumentException("formatDateTime illegal style argument");
            } else {
                DateFormat dateFormat = DateFormat.getDateTimeInstance(style, style, locale);
                return dateFormat.format(date);
            }
        } else {
            throw new IllegalArgumentException("formatDateTime null argument");
        }
    }

    public String formatNumber(Long number, Locale locale) {
        if (number != null && locale != null) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            return numberFormat.format(number);
        } else {
            throw new IllegalArgumentException("formatNumber null argument");
        }
    }

    public String formatNumber(Double number, Locale locale) {
        if (number != null && locale != null) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            return numberFormat.format(number);
        } else {
            throw new IllegalArgumentException("formatNumber null argument");
        }
    }

    public String formatPercent(Double number, Locale locale) {
        if (number != null && locale != null) {
            NumberFormat numberFormat = NumberFormat.getPercentInstance(locale);
            return numberFormat.format(number);
        } else {
            throw new IllegalArgumentException("formatPercent null argument");
        }
    }

    public String formatTime(Date date, int style, Locale locale) {
        if (date != null && locale != null) {
            if (!this.isStyleDateFormatValid(style)) {
                throw new IllegalArgumentException("formatTime illegal style argument");
            } else {
                DateFormat dateFormat = DateFormat.getTimeInstance(style, locale);
                return dateFormat.format(date);
            }
        } else {
            throw new IllegalArgumentException("formatTime null argument");
        }
    }

    public Date parseDate(String date, int style, Locale locale) {
        if (date != null && locale != null) {
            try {
                if (!this.isStyleDateFormatValid(style)) {
                    throw new IllegalArgumentException("parseDate illegal style argument");
                } else {
                    DateFormat dateFormat = DateFormat.getDateInstance(style, locale);
                    return dateFormat.parse(date);
                }
            } catch (ParseException var5) {
                throw new IllegalArgumentException("Parse exception in parse date", var5);
            }
        } else {
            throw new IllegalArgumentException("parseDate null argument");
        }
    }

    public Date parseDateTime(String date, int style, Locale locale) {
        if (date != null && locale != null) {
            try {
                if (!this.isStyleDateFormatValid(style)) {
                    throw new IllegalArgumentException("parseDateTime illegal style argument");
                } else {
                    DateFormat dateFormat = DateFormat.getDateTimeInstance(style, style, locale);
                    return dateFormat.parse(date);
                }
            } catch (ParseException var5) {
                throw new IllegalArgumentException("Parse exception in parse date", var5);
            }
        } else {
            throw new IllegalArgumentException("parseDateTime null argument");
        }
    }

    public Double parseNumberForDouble(String number, Locale locale) {
        if (number != null && locale != null) {
            try {
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                return (Double) numberFormat.parse(number);
            } catch (ParseException var4) {
                throw new IllegalArgumentException("Parse exception in parse number for double", var4);
            }
        } else {
            throw new IllegalArgumentException("parseNumberForDouble null argument");
        }
    }

    public Long parseNumberForLong(String number, Locale locale) {
        if (number != null && locale != null) {
            try {
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                return (Long) numberFormat.parse(number);
            } catch (ParseException var4) {
                throw new IllegalArgumentException("Parse exception in parse number for Long", var4);
            }
        } else {
            throw new IllegalArgumentException("parseNumberForLong null argument");
        }
    }

    public Double parsePercentForDouble(String number, Locale locale) {
        if (number != null && locale != null) {
            try {
                NumberFormat numberFormat = NumberFormat.getPercentInstance(locale);
                return (Double) numberFormat.parse(number);
            } catch (ParseException var4) {
                throw new IllegalArgumentException("Parse exception in parse percent for double", var4);
            }
        } else {
            throw new IllegalArgumentException("parsePercentForDouble null argument");
        }
    }

    public Date parseTime(String date, int style, Locale locale) {
        if (date != null && locale != null) {
            try {
                if (!this.isStyleDateFormatValid(style)) {
                    throw new IllegalArgumentException("parseTime illegal style argument");
                } else {
                    DateFormat dateFormat = DateFormat.getTimeInstance(style, locale);
                    return dateFormat.parse(date);
                }
            } catch (ParseException var5) {
                throw new IllegalArgumentException("Parse exception in parse time", var5);
            }
        } else {
            throw new IllegalArgumentException("parseTime null argument");
        }
    }

    private boolean isStyleDateFormatValid(int style) {
        switch (style) {
            case 0:
                return true;
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return true;
            default:
                return false;
        }
    }
}

