================================================================================
                    USER MANAGEMENT - SPRING BOOT PROJECT
================================================================================

A complete Spring Boot REST API for user management featuring:
- Clean Architecture
- JWT Authentication
- Redis Caching
- Async Processing
- Scheduled Tasks
- Swagger/OpenAPI Documentation

================================================================================
                              PROJECT STRUCTURE
================================================================================

Clean Architecture - 4 Layer Design:

user-management/
├── src/main/java/com/example/usermanagement/
│   │
│   ├── domain/                          # DOMAIN LAYER (Core Business)
│   │   ├── model/
│   │   │   ├── User.java                # User entity
│   │   │   └── AuditLog.java            # Audit log entity
│   │   ├── enums/
│   │   │   └── Role.java                # USER, ADMIN roles
│   │   ├── exception/
│   │   │   ├── DomainException.java     # Base exception
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── ValidationException.java
│   │   │   └── ResourceNotFoundException.java
│   │   └── repository/
│   │       ├── UserRepository.java      # User data access
│   │       └── AuditLogRepository.java  # Audit data access
│   │
│   ├── application/                     # APPLICATION LAYER (Use Cases)
│   │   ├── dto/
│   │   │   ├── user/
│   │   │   │   ├── UserRequestDTO.java
│   │   │   │   └── UserResponseDTO.java
│   │   │   ├── auth/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── AuthResponse.java
│   │   │   └── common/
│   │   │       └── ErrorResponse.java
│   │   ├── mapper/
│   │   │   └── UserMapper.java          # MapStruct mapper
│   │   ├── service/
│   │   │   ├── UserService.java         # User business logic
│   │   │   └── TransactionalDemoService.java
│   │   └── port/input/
│   │       ├── UserServicePort.java     # Service interface
│   │       └── AuthServicePort.java
│   │
│   ├── infrastructure/                  # INFRASTRUCTURE LAYER (External)
│   │   ├── config/
│   │   │   ├── AsyncConfig.java         # Thread pool executors
│   │   │   ├── SchedulerConfig.java     # Scheduled task config
│   │   │   ├── RedisConfig.java         # Cache configuration
│   │   │   └── OpenApiConfig.java       # Swagger config
│   │   ├── security/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java  # Spring Security 6+
│   │   │   ├── jwt/
│   │   │   │   ├── JwtService.java      # Token generation/validation
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   └── service/
│   │   │       ├── AuthService.java     # Login/register logic
│   │   │       └── CustomUserDetailsService.java
│   │   └── scheduling/
│   │       ├── AsyncUserService.java    # Async operations
│   │       └── ScheduledTaskService.java # Cron jobs
│   │
│   ├── presentation/                    # PRESENTATION LAYER (API)
│   │   ├── controller/
│   │   │   ├── UserController.java      # User CRUD endpoints
│   │   │   ├── AuthController.java      # Login/register endpoints
│   │   │   ├── AsyncDemoController.java # Async demo endpoints
│   │   │   └── TransactionDemoController.java
│   │   └── advice/
│   │       └── GlobalExceptionHandler.java
│   │
│   ├── UserManagementApplication.java   # Entry point
│   └── package-info.java                # Architecture documentation
│
├── src/main/resources/
│   └── application.properties           # Configuration
│
├── docker-compose.yml                   # MySQL + Redis containers
├── pom.xml                              # Maven dependencies
├── SPRING_BOOT_LESSON.md               # Comprehensive learning guide
└── readme.txt                           # This file

================================================================================
                              QUICK START
================================================================================

1. Start Docker containers (MySQL + Redis):
   cd user-management
   docker-compose up -d

2. Verify containers are running:
   docker ps

3. Run Spring Boot application:
   ./mvnw spring-boot:run

4. Access Swagger UI:
   http://localhost:8080/swagger-ui.html

5. Register a user:
   POST http://localhost:8080/api/auth/register

================================================================================
                              DOCKER SETUP
================================================================================

SERVICES:
- MySQL 8.0 (Database) - Port 3306
- Redis 7 (Cache) - Port 6379

COMMANDS:
--------------------------------------------------------------------------------
Command                              Description
-------                              -----------
docker-compose up -d                 Start all services
docker-compose down                  Stop all services
docker-compose down -v               Stop and delete all data
docker-compose logs -f               View all logs
docker-compose logs -f mysql         View MySQL logs
docker-compose logs -f redis         View Redis logs
docker-compose ps                    Check status

CONNECT TO MYSQL:
docker exec -it user_management_mysql mysql -u appuser -p

CONNECT TO REDIS:
docker exec -it user_management_redis redis-cli

================================================================================
                          DATABASE CREDENTIALS
================================================================================

Setting         Value
-------         -----
Host            localhost
Port            3306
Database        user_management_db
Username        appuser
Password        apppassword
Root Password   rootpassword

================================================================================
                          REDIS CONFIGURATION
================================================================================

Setting         Value
-------         -----
Host            localhost
Port            6379

Cache Names:
- users         Individual user by ID (TTL: 30 min)
- usersList     All users list (TTL: 5 min)
- userByEmail   User by email lookup (TTL: 30 min)

================================================================================
                              API ENDPOINTS
================================================================================

AUTHENTICATION (No Auth Required)
--------------------------------------------------------------------------------
Method   Endpoint                  Description
------   --------                  -----------
POST     /api/auth/register        Register new user
POST     /api/auth/login           Login and get JWT token
POST     /api/auth/register-admin  Register admin (ADMIN role required)

USER MANAGEMENT (Auth Required)
--------------------------------------------------------------------------------
Method   Endpoint                  Description              Role
------   --------                  -----------              ----
POST     /api/users                Create user              USER, ADMIN
GET      /api/users                Get all users            USER, ADMIN
GET      /api/users/{id}           Get user by ID           USER, ADMIN
PUT      /api/users/{id}           Update user              USER, ADMIN
DELETE   /api/users/{id}           Delete user              ADMIN only

TRANSACTION DEMO
--------------------------------------------------------------------------------
PUT      /api/demo/transactions/users/{id}/email
PUT      /api/demo/transactions/users/{id}/email-with-rollback
PUT      /api/demo/transactions/users/{id}/requires-new
GET      /api/demo/transactions/audit-logs
GET      /api/demo/transactions/audit-logs/user/{userId}

ASYNC DEMO
--------------------------------------------------------------------------------
POST     /api/demo/async/send-email/{userId}
GET      /api/demo/async/users
POST     /api/demo/async/generate-report
GET      /api/demo/async/parallel-demo
POST     /api/demo/async/log-activity/{userId}

DOCUMENTATION
--------------------------------------------------------------------------------
GET      /swagger-ui.html          Swagger UI
GET      /v3/api-docs              OpenAPI JSON

================================================================================
                          AUTHENTICATION FLOW
================================================================================

1. REGISTER
   POST /api/auth/register
   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "password": "password123"
   }

2. LOGIN
   POST /api/auth/login
   {
     "email": "john@example.com",
     "password": "password123"
   }

3. USE TOKEN
   For all protected endpoints, add header:
   Authorization: Bearer <token>

================================================================================
                              API EXAMPLES
================================================================================

1. REGISTER USER
--------------------------------------------------------------------------------
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "password123"
  }'

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER"
}

2. LOGIN
--------------------------------------------------------------------------------
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

3. GET ALL USERS (with token)
--------------------------------------------------------------------------------
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <your-token>"

4. CREATE USER (with token)
--------------------------------------------------------------------------------
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com"
  }'

5. UPDATE USER (with token)
--------------------------------------------------------------------------------
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "firstName": "John",
    "lastName": "Updated",
    "email": "john.updated@example.com"
  }'

6. DELETE USER (ADMIN only)
--------------------------------------------------------------------------------
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <admin-token>"

================================================================================
                              SAMPLE RESPONSES
================================================================================

SUCCESS - USER RESPONSE:
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "createdAt": "2025-12-01T10:30:00",
  "updatedAt": "2025-12-01T10:30:00"
}

ERROR - VALIDATION:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have validation errors",
  "path": "/api/users",
  "fieldErrors": {
    "email": "Please provide a valid email address",
    "firstName": "First name is required"
  }
}

ERROR - NOT FOUND:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999"
}

ERROR - UNAUTHORIZED:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/users"
}

ERROR - FORBIDDEN:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/users/1"
}

================================================================================
                              FEATURES
================================================================================

ARCHITECTURE:
- Clean Architecture (4 layers)
- Domain-Driven Design principles
- Dependency Inversion

SECURITY:
- JWT Authentication
- Spring Security 6+
- Role-based Authorization (USER, ADMIN)
- BCrypt Password Encoding

CACHING:
- Redis Cache
- @Cacheable, @CacheEvict, @CachePut
- Configurable TTL per cache

ASYNC PROCESSING:
- Custom ThreadPoolTaskExecutor
- Multiple executors (task, email, report)
- CompletableFuture support

SCHEDULING:
- @Scheduled with cron expressions
- fixedRate and fixedDelay
- Multi-threaded scheduler

DOCUMENTATION:
- Swagger/OpenAPI 3.0
- Request/Response examples
- Error response examples

VALIDATION:
- Bean Validation (JSR-380)
- Custom exception handling
- Consistent error format

DATA:
- Spring Data JPA
- MySQL 8.0
- Automatic timestamps

================================================================================
                              TECHNOLOGIES
================================================================================

Core:
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security 6+

Database:
- MySQL 8.0
- Redis 7

Libraries:
- JWT (jjwt 0.12.3)
- MapStruct 1.5.5
- Lombok
- SpringDoc OpenAPI 2.2.0

DevOps:
- Docker
- Docker Compose
- Maven

================================================================================
                              LEARNING
================================================================================

For a comprehensive guide on all concepts used in this project, see:

  SPRING_BOOT_LESSON.md

This 1,500+ line guide covers:
- Project setup
- Clean Architecture
- JPA & Repositories
- REST Controllers
- DTOs & MapStruct
- Validation
- Exception Handling
- JWT Authentication
- Spring Security
- Redis Caching
- Async Processing
- Scheduled Tasks
- Transactions
- Swagger Documentation
- Docker Setup

================================================================================
                              REPOSITORY
================================================================================

GitHub: https://github.com/Sosakpanha/Spring-boot

================================================================================
