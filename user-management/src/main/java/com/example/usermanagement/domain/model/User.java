package com.example.usermanagement.domain.model;

import com.example.usermanagement.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Domain Entity representing a User.
 *
 * Part of the DOMAIN LAYER - the core business entity.
 *
 * This entity:
 * - Represents the 'users' table in the database
 * - Implements UserDetails for Spring Security integration
 * - Contains core business attributes
 *
 * Note: In strict Clean Architecture, domain entities shouldn't have
 * JPA annotations. However, for pragmatic Spring Boot development,
 * we keep JPA annotations here to avoid entity duplication.
 */
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

    // ==================== BUSINESS METHODS ====================
    // Domain entities can contain business logic

    /**
     * Check if user has admin role.
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * Get full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ==================== SPRING SECURITY ====================

    /**
     * Returns the authorities granted to the user.
     * Used by Spring Security for authorization.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the username (email in our case).
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Account status checks - all return true for simplicity.
     * Can be extended with actual fields for account management.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
