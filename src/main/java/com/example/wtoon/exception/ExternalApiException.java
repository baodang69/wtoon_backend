package com.example.wtoon.exception;

/**
 * Exception khi gọi API bên ngoài thất bại (500)
 * Sử dụng: throw new ExternalApiException("Failed to fetch chapter content", cause);
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
