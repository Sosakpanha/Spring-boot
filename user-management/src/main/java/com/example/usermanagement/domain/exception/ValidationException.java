package com.example.usermanagement.domain.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown for business validation errors.
 *
 * Part of the DOMAIN LAYER - represents business rule violations.
 *
 * Examples:
 * - Duplicate email address
 * - Invalid business rules
 */
public class ValidationException extends DomainException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    public ValidationException(String field, String message) {
        super(message);
        this.errors = new HashMap<>();
        this.errors.put(field, message);
    }

    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        this.errors.put(field, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
