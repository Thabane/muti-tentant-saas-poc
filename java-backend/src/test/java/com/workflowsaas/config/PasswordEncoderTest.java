package com.workflowsaas.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight unit tests for BCryptPasswordEncoder configuration.
 * Tests password encoding behavior without loading full Spring context.
 */
class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create BCryptPasswordEncoder with work factor 10 (same as SecurityConfig)
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Test
    void testPasswordEncoderEncodesPasswords() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
    }

    @Test
    void testBCryptHashFormat() {
        String rawPassword = "myPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // BCrypt format: $2a$10$... or $2b$10$...
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
                "BCrypt hash should start with $2a$ or $2b$");
    }

    @Test
    void testWorkFactor10ForNodeJsCompatibility() {
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Verify work factor is 10 (for Node.js backend compatibility)
        assertTrue(encodedPassword.matches("\\$2[ab]\\$10\\$.*"),
                "BCrypt should use work factor 10 for Node.js compatibility");
    }

    @Test
    void testPasswordValidation() {
        String rawPassword = "mySecurePassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Password encoder should validate correct password");
    }

    @Test
    void testIncorrectPasswordRejection() {
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword),
                "Password encoder should reject incorrect password");
    }

    @Test
    void testDifferentHashesForSamePassword() {
        String rawPassword = "samePassword";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);
        
        assertNotEquals(hash1, hash2,
                "BCrypt should produce different hashes due to random salt");
        
        // Both hashes should validate the same password
        assertTrue(passwordEncoder.matches(rawPassword, hash1));
        assertTrue(passwordEncoder.matches(rawPassword, hash2));
    }

    @Test
    void testEmptyPasswordHandling() {
        String emptyPassword = "";
        String encodedPassword = passwordEncoder.encode(emptyPassword);
        
        assertNotNull(encodedPassword, "Should encode empty password");
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword),
                "Should validate empty password");
    }

    @Test
    void testSpecialCharactersInPassword() {
        String specialPassword = "p@ssw0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`";
        String encodedPassword = passwordEncoder.encode(specialPassword);
        
        assertNotNull(encodedPassword, "Should encode password with special characters");
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword),
                "Should validate password with special characters");
    }

    @Test
    void testLongPasswordHandling() {
        String longPassword = "a".repeat(100);
        String encodedPassword = passwordEncoder.encode(longPassword);
        
        assertNotNull(encodedPassword, "Should encode long password");
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword),
                "Should validate long password");
    }

    @Test
    void testUnicodeCharactersInPassword() {
        String unicodePassword = "пароль密码🔒";
        String encodedPassword = passwordEncoder.encode(unicodePassword);
        
        assertNotNull(encodedPassword, "Should encode unicode password");
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword),
                "Should validate unicode password");
    }

    @Test
    void testPasswordHashLength() {
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // BCrypt hashes are always 60 characters
        assertEquals(60, encodedPassword.length(),
                "BCrypt hash should be 60 characters long");
    }

    @Test
    void testCaseSensitivePasswordValidation() {
        String rawPassword = "MyPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches("MyPassword", encodedPassword),
                "Should match exact case");
        assertFalse(passwordEncoder.matches("mypassword", encodedPassword),
                "Should not match different case");
        assertFalse(passwordEncoder.matches("MYPASSWORD", encodedPassword),
                "Should not match different case");
    }
}
