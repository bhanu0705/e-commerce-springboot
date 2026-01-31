package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository - Database operations for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (for login, duplicate check)
    Optional<User> findByEmail(String email);

    // Check if email already exists
    boolean existsByEmail(String email);
}
