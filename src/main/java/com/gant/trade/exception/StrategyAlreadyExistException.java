package com.gant.trade.exception;

public class StrategyAlreadyExistException extends BusinessRuntimeException {
    public StrategyAlreadyExistException() {
        this(null, null);
    }

    public StrategyAlreadyExistException(String message) {
        this(message, null);
    }

    public StrategyAlreadyExistException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public StrategyAlreadyExistException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public StrategyAlreadyExistException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public StrategyAlreadyExistException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
