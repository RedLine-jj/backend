package com.jj.redline.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RedlineException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
