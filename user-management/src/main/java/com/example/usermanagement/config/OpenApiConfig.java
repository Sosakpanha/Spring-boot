package com.example.usermanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger UI.
 *
 * Adds JWT Bearer authentication to Swagger UI.
 * After logging in, use the "Authorize" button to add your token.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Management API",
                version = "1.0",
                description = "REST API for User Management with JWT Authentication",
                contact = @Contact(name = "Developer", email = "dev@example.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT token (without 'Bearer ' prefix)"
)
public class OpenApiConfig {
}
