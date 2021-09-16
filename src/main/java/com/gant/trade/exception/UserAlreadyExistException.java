package com.gant.trade.exception;

public class UserAlreadyExistException extends BusinessRuntimeException {
    public UserAlreadyExistException() {
        this(null, null);
    }

    public UserAlreadyExistException(String message) {
        this(message, null);
    }

    public UserAlreadyExistException(Object[] parameters) {
        this(null, null, parameters, null);
    }

    public UserAlreadyExistException(String message, Object[] parameters) {
        this(message, null, parameters, null);
    }

    public UserAlreadyExistException(String message, String hint, Object[] parameters) {
        this(message, hint, parameters, null);
    }

    public UserAlreadyExistException(String message, String hint, Object[] parameters, Throwable cause) {
        super(message, null, null, hint, null, parameters, cause);
    }
}
