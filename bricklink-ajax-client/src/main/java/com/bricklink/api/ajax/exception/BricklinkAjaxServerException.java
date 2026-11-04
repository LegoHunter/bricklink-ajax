package com.bricklink.api.ajax.exception;

public class BricklinkAjaxServerException extends RuntimeException {
    private final Integer statusCode;
    private final String responseBody;

    public BricklinkAjaxServerException(Integer statusCode, String message) {
        this(statusCode, message, null);
    }

    public BricklinkAjaxServerException(Integer statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    /** The raw HTTP response body. */
    public String getResponseBody() {
        return responseBody;
    }
}
