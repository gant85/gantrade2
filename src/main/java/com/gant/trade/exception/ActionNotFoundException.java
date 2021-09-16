package com.gant.trade.exception;

public class ActionNotFoundException extends BusinessRuntimeException {
    public ActionNotFoundException() {
        this(null, null);
    }

    public ActionNotFoundException(String message) {
        this(message, null);
    }

    public ActionNotFoundException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public ActionNotFoundException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public ActionNotFoundException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public ActionNotFoundException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
