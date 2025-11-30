# Spring Boot Complete Lesson Guide

> A comprehensive guide covering Spring Boot development from basics to advanced concepts, based on building a User Management API.

---

## Table of Contents

1. [Project Setup](#1-project-setup)
2. [Clean Architecture](#2-clean-architecture)
3. [JPA Entities & Repositories](#3-jpa-entities--repositories)
4. [Service Layer](#4-service-layer)
5. [REST Controllers](#5-rest-controllers)
6. [DTOs & MapStruct](#6-dtos--mapstruct)
7. [Validation](#7-validation)
8. [Exception Handling](#8-exception-handling)
9. [JWT Authentication](#9-jwt-authentication)
10. [Spring Security 6+](#10-spring-security-6)
11. [Redis Caching](#11-redis-caching)
12. [Async Processing](#12-async-processing)
13. [Scheduled Tasks](#13-scheduled-tasks)
14. [Transactions](#14-transactions)
15. [Swagger/OpenAPI Documentation](#15-swaggeropenapi-documentation)
16. [Docker Setup](#16-docker-setup)

---

## 1. Project Setup

### Maven Dependencies (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>

<dependencies>
    <!-- Core Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- Redis Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### Application Properties

```properties
# Server
server.port=8080

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/user_management_db
spring.datasource.username=appuser
spring.datasource.password=apppassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
```

---

## 2. Clean Architecture

### Overview

Clean Architecture separates code into layers with strict dependency rules:
- Inner layers know NOTHING about outer layers
- Dependencies point INWARD (toward domain)

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│                (Controllers, REST endpoints)                 │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                        │
│                 (Services, DTOs, Use Cases)                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│                (Entities, Business Rules)                    │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                      │
│                (Repositories, External APIs)                 │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.example.usermanagement/
│
├── domain/                    ← INNERMOST (No dependencies)
│   ├── model/                 ← Entities (User, AuditLog)
│   ├── enums/                 ← Enums (Role)
│   ├── exception/             ← Domain exceptions
│   └── repository/            ← Repository interfaces
│
├── application/               ← Depends on domain only
│   ├── dto/                   ← Data Transfer Objects
│   ├── mapper/                ← Entity <-> DTO mappers
│   ├── service/               ← Business logic
│   └── port/                  ← Input/Output ports
│
├── infrastructure/            ← External concerns
│   ├── config/                ← Spring configurations
│   ├── security/              ← Security (JWT, filters)
│   └── scheduling/            ← Async & scheduled tasks
│
└── presentation/              ← API layer
    ├── controller/            ← REST controllers
    └── advice/                ← Exception handlers
```

### Layer Responsibilities

| Layer | Responsibility | Can Depend On |
|-------|---------------|---------------|
| **Domain** | Core business logic, entities | Nothing |
| **Application** | Use cases, orchestration | Domain |
| **Infrastructure** | DB, external services | Domain |
| **Presentation** | HTTP handling | Application |

---

## 3. JPA Entities & Repositories

### Entity Example

```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // UserDetails implementation for Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
```

### Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@Entity` | Marks class as JPA entity |
| `@Table(name = "users")` | Specifies table name |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto-generate ID |
| `@Column` | Column mapping & constraints |
| `@Enumerated(EnumType.STRING)` | Store enum as string |
| `@CreationTimestamp` | Auto-set on create |
| `@UpdateTimestamp` | Auto-update on modify |

### Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA auto-implements based on method name
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Custom query
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") Role role);
}
```

### JpaRepository Methods (Free!)

| Method | Description |
|--------|-------------|
| `save(entity)` | Create or update |
| `findById(id)` | Find by primary key |
| `findAll()` | Get all records |
| `deleteById(id)` | Delete by ID |
| `count()` | Count records |
| `existsById(id)` | Check existence |

---

## 4. Service Layer

### Service Interface (Port)

```java
public interface UserServicePort {
    User createUser(User user);
    User getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
}
```

### Service Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserServicePort {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("email", "Email already exists");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserById(id);

        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
```

### Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@Service` | Marks as Spring service bean |
| `@RequiredArgsConstructor` | Generates constructor for final fields (DI) |
| `@Transactional` | Wraps method in database transaction |
| `@Transactional(readOnly = true)` | Optimized for read operations |

---

## 5. REST Controllers

### Controller Example

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations for users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO request) {
        User user = userMapper.toEntity(request);
        User created = userService.createUser(user);
        return new ResponseEntity<>(userMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toResponseDTOList(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO request) {
        User user = userMapper.toEntity(request);
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### HTTP Method Mapping

| Annotation | HTTP Method | Typical Use |
|------------|-------------|-------------|
| `@GetMapping` | GET | Read data |
| `@PostMapping` | POST | Create resource |
| `@PutMapping` | PUT | Update resource |
| `@DeleteMapping` | DELETE | Delete resource |
| `@PatchMapping` | PATCH | Partial update |

### Response Status Codes

| Status | Meaning | When to Use |
|--------|---------|-------------|
| 200 OK | Success | GET, PUT success |
| 201 Created | Resource created | POST success |
| 204 No Content | Success, no body | DELETE success |
| 400 Bad Request | Invalid input | Validation errors |
| 401 Unauthorized | Not authenticated | Missing/invalid token |
| 403 Forbidden | Not authorized | Insufficient permissions |
| 404 Not Found | Resource missing | Entity not found |
| 500 Internal Error | Server error | Unexpected errors |

---

## 6. DTOs & MapStruct

### Request DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating/updating user")
public class UserRequestDTO {

    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @Schema(description = "Email address", example = "john@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
```

### Response DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload containing user details")
public class UserResponseDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### MapStruct Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    // DTO → Entity
    User toEntity(UserRequestDTO dto);

    // Entity → DTO
    UserResponseDTO toResponseDTO(User entity);

    // List mapping
    List<UserResponseDTO> toResponseDTOList(List<User> entities);

    // Update existing entity
    void updateEntityFromDTO(UserRequestDTO dto, @MappingTarget User entity);
}
```

### Why Use DTOs?

1. **Security**: Don't expose internal entity structure
2. **Flexibility**: Different views for different endpoints
3. **Validation**: Validate input separately from entities
4. **Versioning**: Easier API versioning
5. **Decoupling**: API contract independent of database schema

---

## 7. Validation

### Common Validation Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Not null | `@NotNull` |
| `@NotBlank` | Not null, not empty, not whitespace | `@NotBlank` |
| `@NotEmpty` | Not null, not empty | `@NotEmpty` |
| `@Size` | String/collection size | `@Size(min=2, max=50)` |
| `@Min` / `@Max` | Number range | `@Min(0) @Max(100)` |
| `@Email` | Valid email format | `@Email` |
| `@Pattern` | Regex match | `@Pattern(regexp="^[a-zA-Z]+$")` |
| `@Past` / `@Future` | Date validation | `@Past` |

### Using Validation

```java
// In DTO
public class UserRequestDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "Must be 2-50 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Letters only")
    private String firstName;
}

// In Controller
@PostMapping
public ResponseEntity<UserResponseDTO> createUser(
        @Valid @RequestBody UserRequestDTO request) {
    // @Valid triggers validation
    // Throws MethodArgumentNotValidException if invalid
}
```

---

## 8. Exception Handling

### Custom Exceptions

```java
// Base domain exception
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}

// Specific exception
public class UserNotFoundException extends DomainException {
    private final Long userId;

    public UserNotFoundException(Long userId) {
        super(String.format("User not found with id: %d", userId));
        this.userId = userId;
    }
}

// Validation exception
public class ValidationException extends DomainException {
    private final Map<String, String> errors;

    public ValidationException(String field, String message) {
        super(message);
        this.errors = new HashMap<>();
        this.errors.put(field, message);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields have errors")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Handle not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Catch-all handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### Error Response Format

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;
}
```

---

## 9. JWT Authentication

### How JWT Works

```
┌──────────────────────────────────────────────────────────────┐
│                      JWT AUTHENTICATION FLOW                  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1. LOGIN REQUEST                                            │
│     Client → POST /api/auth/login {email, password}          │
│                                                              │
│  2. VALIDATE CREDENTIALS                                     │
│     Server verifies email/password against database          │
│                                                              │
│  3. GENERATE JWT TOKEN                                       │
│     Server creates signed token with user info & expiry      │
│                                                              │
│  4. RETURN TOKEN                                             │
│     Server → {token: "eyJhbGciOiJIUzI1NiIs..."}             │
│                                                              │
│  5. STORE TOKEN                                              │
│     Client stores token (localStorage, cookie, etc.)         │
│                                                              │
│  6. AUTHENTICATED REQUESTS                                   │
│     Client → Authorization: Bearer <token>                   │
│                                                              │
│  7. VALIDATE TOKEN                                           │
│     Server verifies signature, expiry, extracts user         │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### JWT Token Structure

```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.    ← Header (algorithm, type)
eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwi... ← Payload (claims)
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV...   ← Signature (verification)
```

### JWT Service

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Generate token
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### JWT Authentication Filter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Check if Bearer token present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        // Validate and authenticate
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 10. Spring Security 6+

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    // Public endpoints (no auth required)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless REST API)
            .csrf(AbstractHttpConfigurer::disable)

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // Stateless session (JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Add JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### Method-Level Security

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // Only ADMIN role can access
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // ...
    }

    // Multiple roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id) {
        // ...
    }

    // Check ownership
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/users/{id}/details")
    public ResponseEntity<UserDetails> getUserDetails(@PathVariable Long id) {
        // ...
    }
}
```

---

## 11. Redis Caching

### Cache Configuration

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Default config
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom TTL per cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("usersList", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
```

### Caching Annotations

```java
@Service
public class UserService {

    // Check cache first, execute method if not found
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Always execute, update cache
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, User details) {
        // Update logic...
    }

    // Remove from cache
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // Delete logic...
    }

    // Multiple cache operations
    @Caching(
        put = { @CachePut(value = "users", key = "#result.id") },
        evict = { @CacheEvict(value = "usersList", allEntries = true) }
    )
    public User createUser(User user) {
        // Create logic...
    }
}
```

### Cache Annotations Summary

| Annotation | Behavior | Use Case |
|------------|----------|----------|
| `@Cacheable` | Check cache first, then execute | READ operations |
| `@CachePut` | Always execute, update cache | UPDATE operations |
| `@CacheEvict` | Remove from cache | DELETE operations |
| `@Caching` | Combine multiple operations | Complex scenarios |

### When to Clear Cache

| Operation | Cache Action |
|-----------|--------------|
| CREATE | Evict list caches (new item added) |
| UPDATE | Update specific item, evict lists |
| DELETE | Evict specific item AND lists |

---

## 12. Async Processing

### Enable Async

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core threads (always alive)
        executor.setCorePoolSize(5);

        // Max threads (when queue is full)
        executor.setMaxPoolSize(10);

        // Queue size (waiting tasks)
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async error in {}: {}", method.getName(), ex.getMessage());
        };
    }
}
```

### Async Methods

```java
@Service
@RequiredArgsConstructor
public class AsyncUserService {

    // Fire-and-forget (returns void)
    @Async
    public void sendWelcomeEmail(User user) {
        // Send email in background
        // Caller doesn't wait
    }

    // Using specific executor
    @Async("emailExecutor")
    public void sendPasswordResetEmail(String email) {
        // Uses emailExecutor thread pool
    }

    // Return result via CompletableFuture
    @Async
    public CompletableFuture<List<User>> findAllUsersAsync() {
        List<User> users = userRepository.findAll();
        return CompletableFuture.completedFuture(users);
    }
}

// Usage in controller
@GetMapping("/async-users")
public ResponseEntity<?> getUsers() throws Exception {
    CompletableFuture<List<User>> future = asyncService.findAllUsersAsync();

    // Wait for result (with timeout)
    List<User> users = future.get(10, TimeUnit.SECONDS);

    return ResponseEntity.ok(users);
}
```

### Thread Pool Sizing Guidelines

| Task Type | Core Pool Size | Explanation |
|-----------|---------------|-------------|
| CPU-bound | CPU cores + 1 | Threads waiting during context switch |
| I/O-bound | CPU cores × 2+ | Threads waiting for I/O |
| Mixed | cores × (1 + wait/compute) | Balance based on ratio |

### Async Rules

1. `@Async` methods must be **public**
2. Must be called from **outside** the class (proxy requirement)
3. Self-invocation (`this.asyncMethod()`) runs **synchronously**
4. Return `void` or `CompletableFuture<T>`

---

## 13. Scheduled Tasks

### Enable Scheduling

```java
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);  // Multiple threads for parallel tasks
        scheduler.setThreadNamePrefix("Scheduled-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }
}
```

### Scheduling Options

```java
@Service
public class ScheduledTaskService {

    // Fixed rate: every X ms from START of previous
    @Scheduled(fixedRate = 30000)  // Every 30 seconds
    public void heartbeat() {
        log.info("Heartbeat at {}", LocalDateTime.now());
    }

    // Fixed delay: X ms after END of previous
    @Scheduled(fixedDelay = 60000)  // 60 seconds after last completion
    public void processQueue() {
        // Process items
    }

    // Initial delay before first execution
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void delayedStart() {
        // First run after 10 seconds, then every 60 seconds
    }

    // Cron expression
    @Scheduled(cron = "0 0 2 * * *")  // Daily at 2:00 AM
    public void nightlyCleanup() {
        // Cleanup old records
    }

    @Scheduled(cron = "0 0 9 * * MON")  // Every Monday at 9:00 AM
    public void weeklyReport() {
        // Generate weekly report
    }
}
```

### Cron Expression Format

```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
│ │ │ │ │ ┌───────────── day of week (0-7 or SUN-SAT)
│ │ │ │ │ │
* * * * * *

Examples:
"0 0 * * * *"      = Every hour (at minute 0)
"0 0 0 * * *"      = Every day at midnight
"0 0 9 * * MON-FRI" = Weekdays at 9:00 AM
"0 0/30 * * * *"   = Every 30 minutes
"0 0 9,17 * * *"   = Daily at 9:00 AM and 5:00 PM
```

### Fixed Rate vs Fixed Delay

| Type | Behavior | Use Case |
|------|----------|----------|
| `fixedRate` | Count from START | Heartbeats, monitoring |
| `fixedDelay` | Count from END | Tasks that shouldn't overlap |

---

## 14. Transactions

### Basic Usage

```java
@Service
@Transactional  // All public methods are transactional
public class UserService {

    @Transactional(readOnly = true)  // Optimized for reads
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    // Uses class-level @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

### Transaction Properties

```java
@Transactional(
    readOnly = false,                    // Allow writes
    timeout = 30,                        // 30 second timeout
    isolation = Isolation.READ_COMMITTED,// Isolation level
    propagation = Propagation.REQUIRED,  // Default propagation
    rollbackFor = Exception.class        // Rollback on any exception
)
public void complexOperation() {
    // Multiple database operations
}
```

### Propagation Types

| Type | Behavior |
|------|----------|
| `REQUIRED` (default) | Join existing or create new |
| `REQUIRES_NEW` | Always create new (suspend existing) |
| `SUPPORTS` | Join existing, run non-transactional if none |
| `MANDATORY` | Must have existing transaction |
| `NEVER` | Must NOT have existing transaction |
| `NESTED` | Nested transaction with savepoint |

### Isolation Levels

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|-------|------------|---------------------|--------------|
| `READ_UNCOMMITTED` | ✓ | ✓ | ✓ |
| `READ_COMMITTED` | ✗ | ✓ | ✓ |
| `REPEATABLE_READ` | ✗ | ✗ | ✓ |
| `SERIALIZABLE` | ✗ | ✗ | ✗ |

### Rollback Example

```java
@Service
public class TransactionalDemoService {

    @Transactional  // Rolls back entire transaction on exception
    public void updateUserWithAudit(Long userId, String newEmail) {
        // Update user
        User user = userRepository.findById(userId).orElseThrow();
        user.setEmail(newEmail);
        userRepository.save(user);

        // Create audit log
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action("EMAIL_CHANGE")
                .build();
        auditLogRepository.save(log);

        // If exception here, BOTH operations roll back
        if (someCondition) {
            throw new RuntimeException("Rollback!");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alwaysSaveAuditLog(Long userId, String action) {
        // This runs in a NEW transaction
        // Saves even if calling transaction fails
    }
}
```

---

## 15. Swagger/OpenAPI Documentation

### Configuration

```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User Management API",
        version = "1.0.0",
        description = "REST API for User Management",
        contact = @Contact(name = "Developer", email = "dev@example.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local"),
        @Server(url = "https://api.example.com", description = "Production")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter JWT token"
)
public class OpenApiConfig {
}
```

### Controller Documentation

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "CRUD operations for users")
public class UserController {

    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided details"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-12-01T10:30:00",
                      "status": 400,
                      "error": "Validation Failed",
                      "message": "Invalid input",
                      "fieldErrors": {
                        "email": "Invalid email format"
                      }
                    }
                    """)
            )
        )
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO request) {
        // ...
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        // ...
    }
}
```

### DTO Documentation

```java
@Schema(description = "Request payload for creating a user")
public class UserRequestDTO {

    @Schema(
        description = "User's first name",
        example = "John",
        required = true,
        minLength = 2,
        maxLength = 50
    )
    private String firstName;
}
```

### Access Swagger UI

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 16. Docker Setup

### docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: user_management_mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: user_management_db
      MYSQL_USER: appuser
      MYSQL_PASSWORD: apppassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: user_management_redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql_data:
    driver: local
  redis_data:
    driver: local
```

### Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Check status
docker-compose ps
```

---

## Quick Reference

### Common Annotations

| Annotation | Layer | Purpose |
|------------|-------|---------|
| `@SpringBootApplication` | Main | Entry point |
| `@RestController` | Controller | REST endpoint class |
| `@Service` | Service | Business logic |
| `@Repository` | Repository | Data access |
| `@Entity` | Domain | JPA entity |
| `@Configuration` | Config | Spring configuration |
| `@Component` | Any | Generic Spring bean |

### Request Mapping

| Annotation | HTTP Method |
|------------|-------------|
| `@GetMapping` | GET |
| `@PostMapping` | POST |
| `@PutMapping` | PUT |
| `@DeleteMapping` | DELETE |
| `@PatchMapping` | PATCH |

### Validation

| Annotation | Purpose |
|------------|---------|
| `@NotNull` | Not null |
| `@NotBlank` | Not empty string |
| `@Size` | Length constraints |
| `@Email` | Email format |
| `@Pattern` | Regex match |

### Caching

| Annotation | Behavior |
|------------|----------|
| `@Cacheable` | Read from cache |
| `@CachePut` | Update cache |
| `@CacheEvict` | Remove from cache |

### Security

| Annotation | Purpose |
|------------|---------|
| `@PreAuthorize` | Method-level authorization |
| `@Secured` | Role-based access |
| `@EnableWebSecurity` | Enable security |
| `@EnableMethodSecurity` | Enable method security |

---

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

> **Note**: This lesson is generated based on hands-on project development. For the complete source code, visit: https://github.com/Sosakpanha/Spring-boot
