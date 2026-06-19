package com.bricklink.api.ajax.exception;

public class BricklinkAjaxServerException extends RuntimeException {
    private final Integer statusCode;

    public BricklinkAjaxServerException(Integer statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
