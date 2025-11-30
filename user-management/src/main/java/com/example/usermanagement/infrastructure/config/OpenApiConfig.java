package com.example.usermanagement.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 Configuration for Swagger UI.
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         SWAGGER UI DOCUMENTATION                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  ACCESS SWAGGER UI:                                                          ║
 * ║  • URL: http://localhost:8080/swagger-ui.html                               ║
 * ║  • API Docs: http://localhost:8080/v3/api-docs                              ║
 * ║                                                                              ║
 * ║  AUTHENTICATION FLOW:                                                        ║
 * ║  1. Register: POST /api/auth/register (no auth required)                    ║
 * ║  2. Login: POST /api/auth/login (no auth required)                          ║
 * ║  3. Copy the JWT token from response                                        ║
 * ║  4. Click "Authorize" button in Swagger UI                                  ║
 * ║  5. Enter token (without "Bearer " prefix)                                  ║
 * ║  6. All protected endpoints now accessible                                   ║
 * ║                                                                              ║
 * ║  KEY ANNOTATIONS:                                                            ║
 * ║  • @OpenAPIDefinition: Global API info (title, version, servers)            ║
 * ║  • @SecurityScheme: Defines authentication method (JWT Bearer)              ║
 * ║  • @Tag: Groups related endpoints together                                  ║
 * ║  • @Operation: Documents individual endpoint                                ║
 * ║  • @ApiResponse: Documents possible responses                               ║
 * ║  • @Schema: Documents DTOs and their fields                                 ║
 * ║  • @Parameter: Documents path/query parameters                              ║
 * ║  • @ExampleObject: Provides JSON examples                                   ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Management API",
                version = "1.0.0",
                description = """
                        ## Overview
                        REST API for User Management with JWT Authentication, Redis Caching, and Async Processing.

                        ## Features
                        - **User CRUD Operations**: Create, Read, Update, Delete users
                        - **JWT Authentication**: Secure endpoints with Bearer token
                        - **Role-Based Access**: USER and ADMIN roles
                        - **Redis Caching**: Cached responses for better performance
                        - **Async Processing**: Background tasks for emails and reports
                        - **Scheduled Jobs**: Automated cleanup and monitoring

                        ## Authentication
                        1. Register a new user at `/api/auth/register`
                        2. Login at `/api/auth/login` to get JWT token
                        3. Include token in Authorization header: `Bearer <token>`

                        ## Error Handling
                        All errors return a consistent format with:
                        - `timestamp`: When the error occurred
                        - `status`: HTTP status code
                        - `error`: Error type
                        - `message`: Human-readable message
                        - `path`: API endpoint
                        - `fieldErrors`: Validation errors (when applicable)
                        """,
                contact = @Contact(
                        name = "API Support",
                        email = "support@example.com",
                        url = "https://github.com/Sosakpanha/Spring-boot"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
                @Server(url = "https://api.example.com", description = "Production Server")
        },
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {
                @Tag(name = "Authentication", description = "Register and login endpoints (no auth required)"),
                @Tag(name = "User Management", description = "CRUD operations for users (auth required)"),
                @Tag(name = "Transaction Demo", description = "Demonstrates @Transactional behavior"),
                @Tag(name = "Async Demo", description = "Demonstrates @Async behavior"),
                @Tag(name = "Admin Operations", description = "Admin-only endpoints (ADMIN role required)")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = """
                JWT Authentication using Bearer token.

                **How to authenticate:**
                1. Login via POST /api/auth/login
                2. Copy the `token` from the response
                3. Click "Authorize" button above
                4. Enter the token (without "Bearer " prefix)
                5. Click "Authorize" to save

                **Token Format:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

                **Note:** Token expires in 24 hours
                """
)
public class OpenApiConfig {
    // Configuration is done via annotations
    // No additional beans needed for basic setup
}
