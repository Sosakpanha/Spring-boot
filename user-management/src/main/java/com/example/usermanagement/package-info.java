/**
 * User Management Application - Clean Architecture
 *
 * ╔══════════════════════════════════════════════════════════════════════════════════════╗
 * ║                              CLEAN ARCHITECTURE OVERVIEW                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                                      ║
 * ║  Clean Architecture separates concerns into layers with strict dependency rules:     ║
 * ║  • Inner layers know NOTHING about outer layers                                      ║
 * ║  • Dependencies point INWARD (toward domain)                                         ║
 * ║  • Business logic is isolated and testable                                           ║
 * ║                                                                                      ║
 * ║                        ┌─────────────────────────────────┐                           ║
 * ║                        │      PRESENTATION LAYER         │                           ║
 * ║                        │   (Controllers, REST endpoints) │                           ║
 * ║                        └────────────────┬────────────────┘                           ║
 * ║                                         │                                            ║
 * ║                                         ▼                                            ║
 * ║                        ┌─────────────────────────────────┐                           ║
 * ║                        │      APPLICATION LAYER          │                           ║
 * ║                        │  (Services, DTOs, Use Cases)    │                           ║
 * ║                        └────────────────┬────────────────┘                           ║
 * ║                                         │                                            ║
 * ║                                         ▼                                            ║
 * ║                        ┌─────────────────────────────────┐                           ║
 * ║                        │        DOMAIN LAYER             │                           ║
 * ║                        │ (Entities, Business Rules)      │                           ║
 * ║                        └────────────────┬────────────────┘                           ║
 * ║                                         │                                            ║
 * ║                                         ▼                                            ║
 * ║                        ┌─────────────────────────────────┐                           ║
 * ║                        │     INFRASTRUCTURE LAYER        │                           ║
 * ║                        │ (Repositories, External APIs)   │                           ║
 * ║                        └─────────────────────────────────┘                           ║
 * ║                                                                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                                      ║
 * ║  PACKAGE STRUCTURE:                                                                  ║
 * ║  ==================                                                                  ║
 * ║                                                                                      ║
 * ║  com.example.usermanagement/                                                         ║
 * ║  │                                                                                   ║
 * ║  ├── domain/                    ← INNERMOST LAYER (No dependencies)                 ║
 * ║  │   ├── model/                 ← Domain entities (User, AuditLog)                  ║
 * ║  │   ├── enums/                 ← Domain enums (Role)                               ║
 * ║  │   ├── exception/             ← Domain exceptions                                 ║
 * ║  │   └── repository/            ← Repository INTERFACES (not implementations)       ║
 * ║  │                                                                                   ║
 * ║  ├── application/               ← APPLICATION LAYER (Depends on domain)             ║
 * ║  │   ├── dto/                   ← Data Transfer Objects                             ║
 * ║  │   ├── mapper/                ← Entity <-> DTO mappers                            ║
 * ║  │   ├── service/               ← Business logic / Use cases                        ║
 * ║  │   └── port/                  ← Input/Output ports (interfaces)                   ║
 * ║  │                                                                                   ║
 * ║  ├── infrastructure/            ← INFRASTRUCTURE LAYER (External concerns)          ║
 * ║  │   ├── persistence/           ← JPA repository implementations                    ║
 * ║  │   ├── config/                ← Spring configurations                             ║
 * ║  │   ├── security/              ← Security (JWT, filters, config)                   ║
 * ║  │   └── scheduling/            ← Scheduled tasks                                   ║
 * ║  │                                                                                   ║
 * ║  └── presentation/              ← PRESENTATION LAYER (API layer)                    ║
 * ║      ├── controller/            ← REST controllers                                  ║
 * ║      └── advice/                ← Global exception handlers                         ║
 * ║                                                                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                                      ║
 * ║  LAYER RESPONSIBILITIES:                                                             ║
 * ║  =======================                                                             ║
 * ║                                                                                      ║
 * ║  1. DOMAIN LAYER (Core Business Logic)                                               ║
 * ║     • Contains enterprise business rules                                             ║
 * ║     • Entities with business logic methods                                           ║
 * ║     • Domain exceptions for business rule violations                                 ║
 * ║     • Repository interfaces (contracts, not implementations)                         ║
 * ║     • NO framework dependencies (pure Java)                                          ║
 * ║                                                                                      ║
 * ║  2. APPLICATION LAYER (Use Cases)                                                    ║
 * ║     • Orchestrates domain entities                                                   ║
 * ║     • Contains application-specific business rules                                   ║
 * ║     • DTOs for data transfer between layers                                          ║
 * ║     • Mappers to convert between entities and DTOs                                   ║
 * ║     • Service interfaces and implementations                                         ║
 * ║                                                                                      ║
 * ║  3. INFRASTRUCTURE LAYER (External Concerns)                                         ║
 * ║     • Database access (JPA repositories)                                             ║
 * ║     • External service integrations                                                  ║
 * ║     • Framework configurations (Spring, Security)                                    ║
 * ║     • Caching, messaging, scheduling                                                 ║
 * ║                                                                                      ║
 * ║  4. PRESENTATION LAYER (User Interface)                                              ║
 * ║     • REST controllers                                                               ║
 * ║     • Request/response handling                                                      ║
 * ║     • Input validation                                                               ║
 * ║     • Exception handling (ControllerAdvice)                                          ║
 * ║                                                                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                                      ║
 * ║  DEPENDENCY RULE:                                                                    ║
 * ║  ================                                                                    ║
 * ║                                                                                      ║
 * ║  Presentation → Application → Domain ← Infrastructure                               ║
 * ║                                                                                      ║
 * ║  • Presentation depends on Application                                               ║
 * ║  • Application depends on Domain                                                     ║
 * ║  • Infrastructure depends on Domain (implements interfaces)                          ║
 * ║  • Domain depends on NOTHING                                                         ║
 * ║                                                                                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════════════╝
 */
package com.example.usermanagement;
