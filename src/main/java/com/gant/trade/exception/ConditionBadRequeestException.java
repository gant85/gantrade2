package com.gant.trade.exception;

public class ConditionBadRequeestException extends BusinessRuntimeException {
    public ConditionBadRequeestException() {
        this(null, null);
    }

    public ConditionBadRequeestException(String message) {
        this(message, null);
    }

    public ConditionBadRequeestException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public ConditionBadRequeestException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public ConditionBadRequeestException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public ConditionBadRequeestException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
