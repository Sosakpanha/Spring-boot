================================================================================
                    USER MANAGEMENT - SPRING BOOT PROJECT
================================================================================

A Spring Boot CRUD API for managing users with MySQL database.

================================================================================
                              PROJECT STRUCTURE
================================================================================

user-management/
├── src/main/java/com/example/usermanagement/
│   ├── UserManagementApplication.java    # Entry point
│   ├── controller/
│   │   └── UserController.java           # REST API endpoints
│   ├── service/
│   │   ├── UserService.java              # Service interface
│   │   └── impl/
│   │       └── UserServiceImpl.java      # Business logic
│   ├── repository/
│   │   └── UserRepository.java           # Data access layer
│   ├── entity/
│   │   └── User.java                     # JPA entity
│   ├── dto/
│   │   ├── UserRequestDTO.java           # Input validation
│   │   └── UserResponseDTO.java          # API response
│   └── exception/
│       ├── ResourceNotFoundException.java
│       └── GlobalExceptionHandler.java   # Error handling
├── src/main/resources/
│   └── application.properties            # Configuration
├── docker-compose.yml                    # MySQL container
└── pom.xml                               # Dependencies

================================================================================
                              DOCKER SETUP
================================================================================

1. Start MySQL container:
   cd user-management
   docker-compose up -d

2. Verify container is running:
   docker ps

3. Check logs (optional):
   docker-compose logs -f mysql

4. Run Spring Boot application:
   ./mvnw spring-boot:run

--------------------------------------------------------------------------------
                          USEFUL DOCKER COMMANDS
--------------------------------------------------------------------------------

Command                                    Description
-------                                    -----------
docker-compose up -d                       Start MySQL in background
docker-compose down                        Stop MySQL
docker-compose down -v                     Stop MySQL and delete data
docker-compose logs -f                     View logs
docker exec -it user_management_mysql      Connect to MySQL CLI
  mysql -u appuser -p

--------------------------------------------------------------------------------
                          DATABASE CREDENTIALS
--------------------------------------------------------------------------------

Setting      Value
-------      -----
Host         localhost
Port         3306
Database     user_management_db
Username     appuser
Password     apppassword

================================================================================
                              API ENDPOINTS
================================================================================

Method   Endpoint           Description
------   --------           -----------
POST     /api/users         Create a new user
GET      /api/users         Get all users
GET      /api/users/{id}    Get user by ID
PUT      /api/users/{id}    Update user
DELETE   /api/users/{id}    Delete user

================================================================================
                              API EXAMPLES
================================================================================

1. CREATE USER
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}'

2. GET ALL USERS
   curl http://localhost:8080/api/users

3. GET USER BY ID
   curl http://localhost:8080/api/users/1

4. UPDATE USER
   curl -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com"}'

5. DELETE USER
   curl -X DELETE http://localhost:8080/api/users/1

================================================================================
                              SAMPLE RESPONSES
================================================================================

CREATE/GET USER RESPONSE:
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "createdAt": "2025-12-01T10:30:00",
  "updatedAt": "2025-12-01T10:30:00"
}

VALIDATION ERROR RESPONSE:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Please provide a valid email address",
    "firstName": "First name is required"
  }
}

NOT FOUND ERROR RESPONSE:
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: '999'"
}

================================================================================
                              TECHNOLOGIES
================================================================================

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL 8.0
- Lombok
- Docker

================================================================================
