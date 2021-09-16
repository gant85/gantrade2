package com.gant.trade.exception;

import org.springframework.util.StringUtils;

public class BusinessRuntimeExceptionBuilder {
    private String message;
    private String code;
    private String status;
    private String hint;
    private Integer level;
    private Object[] parameters;
    private Throwable cause;

    protected BusinessRuntimeExceptionBuilder() {
    }

    public BusinessRuntimeExceptionBuilder setCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setLevel(Integer level) {
        this.level = level;
        return this;
    }

    public BusinessRuntimeExceptionBuilder setParameters(Object[] parameters) {
        this.parameters = parameters;
        return this;
    }

    public BusinessRuntimeException build() {
        if (StringUtils.isEmpty(this.code)) {
            throw new IllegalArgumentException("BusinessRuntimeException code cannot be null");
        } else {
            return new BusinessRuntimeException(this.message, this.code, this.status, this.hint, this.level, this.parameters, this.cause);
        }
    }
}
