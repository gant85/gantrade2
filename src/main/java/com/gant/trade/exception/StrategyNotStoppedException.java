package com.gant.trade.exception;

public class StrategyNotStoppedException extends BusinessRuntimeException {
    public StrategyNotStoppedException() {
        this(null, null);
    }

    public StrategyNotStoppedException(String message) {
        this(message, null);
    }

    public StrategyNotStoppedException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public StrategyNotStoppedException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public StrategyNotStoppedException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public StrategyNotStoppedException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
