package com.example.wtoon.handler;

import com.example.wtoon.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice // Xử lý ngoại lệ toàn cục
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Bắt lỗi 404 (Không tìm thấy)
     * Kích hoạt khi gọi .orElseThrow() hoặc ném ResponseStatusException(HttpStatus.NOT_FOUND)
     */
    @ExceptionHandler({NoSuchElementException.class, ResponseStatusException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String message = ex.getMessage();

        if (ex instanceof ResponseStatusException) {
            // Lấy message từ ResponseStatusException
            message = ((ResponseStatusException) ex).getReason();
        }

        return buildResponseEntity(HttpStatus.NOT_FOUND, message, path);
    }

    /**
     * Bắt lỗi 500 (Lỗi server chung, ví dụ lỗi Crawl On-Demand)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.error("Runtime Error: ", ex); // Ghi log lỗi chi tiết
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), path);
    }

    /**
     * Bắt tất cả các lỗi 500 khác
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.error("Internal server error: ", ex);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", path);
    }

    // Hàm helper để xây dựng ErrorResponse chuẩn
    private ResponseEntity<ErrorResponse> buildResponseEntity(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}