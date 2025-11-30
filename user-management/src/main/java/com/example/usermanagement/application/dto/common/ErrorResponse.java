package com.example.usermanagement.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response format for all API errors.
 *
 * Part of the APPLICATION LAYER - used for consistent error responses.
 *
 * Provides consistent structure for error handling across the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Timestamp when error occurred", example = "2025-12-01T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "API path where error occurred", example = "/api/users")
    private String path;

    @Schema(description = "Field-level validation errors (only for validation failures)")
    private Map<String, String> fieldErrors;
}
