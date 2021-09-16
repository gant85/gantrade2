package com.gant.trade.exception;

public class IndicatorNotFoundException extends BusinessRuntimeException {
    public IndicatorNotFoundException() {
        this(null, null);
    }

    public IndicatorNotFoundException(String message) {
        this(message, null);
    }

    public IndicatorNotFoundException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public IndicatorNotFoundException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public IndicatorNotFoundException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public IndicatorNotFoundException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
