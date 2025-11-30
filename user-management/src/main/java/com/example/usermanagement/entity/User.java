package com.example.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'users' table in the database.
 *
 * @Entity: Marks this class as a JPA entity (database table)
 * @Table: Specifies the table name in the database
 * @Data: Lombok annotation that generates getters, setters, toString, equals, hashCode
 * @Builder: Enables the builder pattern for object creation
 * @NoArgsConstructor: Generates a no-argument constructor (required by JPA)
 * @AllArgsConstructor: Generates a constructor with all fields
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primary key with auto-increment strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's first name - required field.
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * User's last name - required field.
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * User's email - must be unique and required.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Timestamp when the record was created.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     * Automatically updated by Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
