package com.jj.redline.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RedlineException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
