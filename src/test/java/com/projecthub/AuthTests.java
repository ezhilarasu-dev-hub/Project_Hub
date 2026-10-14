package com.projecthub;

import com.projecthub.dto.AuthResponse;
import com.projecthub.dto.LoginRequest;
import com.projecthub.dto.RegisterRequest;
import com.projecthub.entity.Role;
import com.projecthub.entity.User;
import com.projecthub.repository.UserRepository;
import com.projecthub.security.JwtTokenProvider;
import com.projecthub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        // DataInitializer sets up default accounts: faculty@institution.edu, student@institution.edu
    }

    @Test
    void testSuccessfulLogin() {
        LoginRequest req = new LoginRequest("student@institution.edu", "student123");
        AuthResponse response = userService.login(req);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("student@institution.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());

        // Validate JWT token
        assertTrue(tokenProvider.validateToken(response.getToken()));
        assertEquals("student@institution.edu", tokenProvider.getUsernameFromToken(response.getToken()));
    }

    @Test
    void testFailedLoginInvalidPassword() {
        LoginRequest req = new LoginRequest("student@institution.edu", "wrongpassword");
        assertThrows(BadCredentialsException.class, () -> userService.login(req));
    }

    @Test
    void testRegisterNewStudent() {
        RegisterRequest req = new RegisterRequest("New Student", "newstudent@institution.edu", "password123", Role.STUDENT);
        AuthResponse response = userService.register(req);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("newstudent@institution.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());

        User user = userRepository.findByEmail("newstudent@institution.edu").orElse(null);
        assertNotNull(user);
        assertEquals("New Student", user.getName());
    }

    @Test
    void testRegisterDuplicateEmailFails() {
        RegisterRequest req = new RegisterRequest("Duplicate", "student@institution.edu", "password123", Role.STUDENT);
        assertThrows(RuntimeException.class, () -> userService.register(req));
    }
}
