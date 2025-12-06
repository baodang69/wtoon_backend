package com.example.wtoon.exception;

/**
 * Exception khi request không hợp lệ (400)
 * Sử dụng: throw new BadRequestException("Invalid page number");
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
