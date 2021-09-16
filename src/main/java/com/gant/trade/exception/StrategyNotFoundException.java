package com.gant.trade.exception;

public class StrategyNotFoundException extends BusinessRuntimeException {
    public StrategyNotFoundException() {
        this(null, null);
    }

    public StrategyNotFoundException(String message) {
        this(message, null);
    }

    public StrategyNotFoundException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public StrategyNotFoundException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public StrategyNotFoundException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public StrategyNotFoundException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
