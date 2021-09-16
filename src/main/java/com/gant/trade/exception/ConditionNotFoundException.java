package com.gant.trade.exception;

public class ConditionNotFoundException extends BusinessRuntimeException {
    public ConditionNotFoundException() {
        this(null, null);
    }

    public ConditionNotFoundException(String message) {
        this(message, null);
    }

    public ConditionNotFoundException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public ConditionNotFoundException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public ConditionNotFoundException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public ConditionNotFoundException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
