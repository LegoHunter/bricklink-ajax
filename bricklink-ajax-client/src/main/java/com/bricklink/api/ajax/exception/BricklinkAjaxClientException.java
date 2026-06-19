package com.bricklink.api.ajax.exception;

public class BricklinkAjaxClientException extends RuntimeException {
    private final Integer statusCode;

    public BricklinkAjaxClientException(String message) {
        super(message);
        this.statusCode = null;
    }

    public BricklinkAjaxClientException(Integer statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
