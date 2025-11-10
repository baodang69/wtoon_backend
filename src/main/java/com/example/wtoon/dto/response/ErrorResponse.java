package com.example.wtoon.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}