package com.lessondashboard.repository;

import com.lessondashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository - Data access for User entities.
 *
 * findByUsername is a custom query method. Spring Data JPA automatically
 * generates the SQL based on the method name:
 *   "findByUsername" → SELECT * FROM app_user WHERE username = ?
 *
 * Returns Optional<User> because the user might not exist.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username (for login).
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
}
