package com.jj.redline.exception;

import org.springframework.http.HttpStatus;

public class CrawlingException extends RedlineException {

    public CrawlingException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public CrawlingException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }
}
