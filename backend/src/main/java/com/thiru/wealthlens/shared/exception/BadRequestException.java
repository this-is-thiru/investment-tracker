package com.thiru.wealthlens.shared.exception;

public class BadRequestException extends IllegalArgumentException {

    public BadRequestException(String message) {
        super(message);
    }
}
