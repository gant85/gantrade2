package com.gant.trade.exception;

public class BusinessRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 5576878029081005630L;
    protected String code;
    protected String status;
    protected String hint;
    protected Integer level;
    protected Object[] parameters;

    protected BusinessRuntimeException(String message, String code, String status, String hint, Integer level, Object[] parameters, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.hint = hint;
        this.level = level;
        this.parameters = parameters;
    }

    public String getCode() {
        return this.code;
    }

    public String getStatus() {
        return this.status;
    }

    public String getHint() {
        return this.hint;
    }

    public Integer getLevel() {
        return this.level;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        return "BusinessRuntimeException [message=" + this.getMessage() + ", code=" + this.code + ", status=" + this.status + ", hint=" + this.hint + ", level=" + this.level + ", parameters=" + this.parameters + "]";
    }

    public static BusinessRuntimeExceptionBuilder builder() {
        return new BusinessRuntimeExceptionBuilder();
    }
}
