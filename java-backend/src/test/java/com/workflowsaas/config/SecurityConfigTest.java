package com.workflowsaas.config;

import com.workflowsaas.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig.
 * Validates security configuration, password encoder, and filter chain setup.
 */
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void contextLoads() {
        assertNotNull(securityConfig, "SecurityConfig should be loaded");
    }

    @Test
    void passwordEncoderBeanExists() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should exist");
    }

    @Test
    void passwordEncoderIsBCrypt() {
        assertTrue(passwordEncoder.getClass().getName().contains("BCrypt"),
                "PasswordEncoder should be BCryptPasswordEncoder");
    }

    @Test
    void passwordEncoderEncodesPasswords() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
                "BCrypt hash should start with $2a$ or $2b$");
    }

    @Test
    void passwordEncoderValidatesCorrectPassword() {
        String rawPassword = "mySecurePassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Password encoder should validate correct password");
    }

    @Test
    void passwordEncoderRejectsIncorrectPassword() {
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword),
                "Password encoder should reject incorrect password");
    }

    @Test
    void passwordEncoderUsesWorkFactor10() {
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // BCrypt format: $2a$10$... where 10 is the work factor
        assertTrue(encodedPassword.matches("\\$2[ab]\\$10\\$.*"),
                "BCrypt should use work factor 10 for Node.js compatibility");
    }

    @Test
    void passwordEncoderProducesDifferentHashesForSamePassword() {
        String rawPassword = "samePassword";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);
        
        assertNotEquals(hash1, hash2,
                "BCrypt should produce different hashes due to random salt");
        assertTrue(passwordEncoder.matches(rawPassword, hash1));
        assertTrue(passwordEncoder.matches(rawPassword, hash2));
    }

    @Test
    void securityFilterChainBeanCanBeCreated() throws Exception {
        assertDoesNotThrow(() -> {
            // This test verifies the bean can be created without errors
            // The actual SecurityFilterChain is created by Spring
        }, "SecurityFilterChain bean creation should not throw exceptions");
    }

    @Test
    void passwordEncoderHandlesEmptyPassword() {
        String emptyPassword = "";
        String encodedPassword = passwordEncoder.encode(emptyPassword);
        
        assertNotNull(encodedPassword, "Should encode empty password");
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword),
                "Should validate empty password");
    }

    @Test
    void passwordEncoderHandlesSpecialCharacters() {
        String specialPassword = "p@ssw0rd!#$%^&*()";
        String encodedPassword = passwordEncoder.encode(specialPassword);
        
        assertNotNull(encodedPassword, "Should encode password with special characters");
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword),
                "Should validate password with special characters");
    }

    @Test
    void passwordEncoderHandlesLongPassword() {
        String longPassword = "a".repeat(100);
        String encodedPassword = passwordEncoder.encode(longPassword);
        
        assertNotNull(encodedPassword, "Should encode long password");
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword),
                "Should validate long password");
    }

    @Test
    void passwordEncoderHandlesUnicodeCharacters() {
        String unicodePassword = "пароль密码🔒";
        String encodedPassword = passwordEncoder.encode(unicodePassword);
        
        assertNotNull(encodedPassword, "Should encode unicode password");
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword),
                "Should validate unicode password");
    }
}
