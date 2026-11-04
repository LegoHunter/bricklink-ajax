package com.bricklink.api.ajax.exception;

public class BricklinkAjaxClientException extends RuntimeException {
    private final Integer statusCode;
    private final String responseBody;

    public BricklinkAjaxClientException(String message) {
        this(null, message, null);
    }

    public BricklinkAjaxClientException(Integer statusCode, String message) {
        this(statusCode, message, null);
    }

    public BricklinkAjaxClientException(Integer statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    /** The raw HTTP response body, when the exception came from an HTTP response. */
    public String getResponseBody() {
        return responseBody;
    }
}
