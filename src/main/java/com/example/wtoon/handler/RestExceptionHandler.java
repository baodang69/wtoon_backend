package com.example.wtoon.handler;

import com.example.wtoon.dto.response.ErrorResponseDTO;
import com.example.wtoon.exception.BadRequestException;
import com.example.wtoon.exception.ExternalApiException;
import com.example.wtoon.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global Exception Handler - Xử lý tất cả exception trong ứng dụng
 * 
 * Cách hoạt động:
 * 1. Khi exception được throw từ Controller/Service
 * 2. Spring tìm @ExceptionHandler phù hợp nhất (theo thứ tự cụ thể -> chung)
 * 3. Handler trả về ErrorResponse chuẩn
 */
@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    /**
     * 404 - Resource không tồn tại
     * Trigger: throw new ResourceNotFoundException("Story", "slug", "abc")
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * 400 - Request không hợp lệ
     * Trigger: throw new BadRequestException("Invalid parameter")
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(
            BadRequestException ex, WebRequest request) {
        
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 - Validation lỗi (từ @Valid annotation)
     * Trigger: Khi DTO có @NotNull, @Size... bị vi phạm
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, errors, request);
    }

    /**
     * 503 - Lỗi khi gọi API bên ngoài
     * Trigger: throw new ExternalApiException("Failed to fetch data")
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalApiError(
            ExternalApiException ex, WebRequest request) {
        
        log.error("External API error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, 
                "External service unavailable: " + ex.getMessage(), request);
    }

    /**
     * 500 - Lỗi không xác định (catch-all)
     * Trigger: Bất kỳ exception nào không được handle ở trên
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred", request);
    }

    // Helper method
    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status, String message, WebRequest request) {
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponseDTO error = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return new ResponseEntity<>(error, status);
    }
}
