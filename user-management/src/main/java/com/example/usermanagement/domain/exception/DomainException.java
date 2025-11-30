package com.example.usermanagement.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 *
 * Part of the DOMAIN LAYER - no framework dependencies.
 *
 * All domain exceptions should extend this class.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
