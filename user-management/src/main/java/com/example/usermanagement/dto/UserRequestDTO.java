package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for incoming user requests (Create/Update).
 *
 * Purpose of DTOs:
 * - Separate API contract from internal entity structure
 * - Apply validation rules on incoming data
 * - Don't expose internal fields (like id, timestamps)
 * - Allow API to evolve independently of database schema
 *
 * Validation annotations ensure data integrity:
 * - @NotBlank: Field cannot be null or empty
 * - @Size: Limits string length
 * - @Email: Must be valid email format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
}
