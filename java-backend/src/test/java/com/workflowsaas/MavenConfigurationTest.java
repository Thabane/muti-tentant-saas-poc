package com.workflowsaas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Maven configuration and dependencies are correctly set up.
 */
class MavenConfigurationTest {

    @Test
    void testSpringBootVersionIsAvailable() {
        String version = SpringBootVersion.getVersion();
        assertNotNull(version, "Spring Boot version should be available");
        assertTrue(version.startsWith("3.2"), "Spring Boot version should be 3.2.x");
    }

    @Test
    void testJavaVersionIsCorrect() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        // Java 17 or higher
        assertTrue(javaVersion.startsWith("17") || javaVersion.startsWith("21") || javaVersion.startsWith("22"),
                "Java version should be 17 or higher, but was: " + javaVersion);
    }

    @Test
    void testRequiredDependenciesAreOnClasspath() {
        // Test Spring Boot dependencies
        assertDoesNotThrow(() -> Class.forName("org.springframework.boot.SpringApplication"),
                "Spring Boot should be on classpath");
        
        // Test Spring Data JPA
        assertDoesNotThrow(() -> Class.forName("org.springframework.data.jpa.repository.JpaRepository"),
                "Spring Data JPA should be on classpath");
        
        // Test Spring Security
        assertDoesNotThrow(() -> Class.forName("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity"),
                "Spring Security should be on classpath");
        
        // Test PostgreSQL driver
        assertDoesNotThrow(() -> Class.forName("org.postgresql.Driver"),
                "PostgreSQL driver should be on classpath");
        
        // Test JWT library
        assertDoesNotThrow(() -> Class.forName("io.jsonwebtoken.Jwts"),
                "JWT library should be on classpath");
        
        // Test Lombok
        assertDoesNotThrow(() -> Class.forName("lombok.Data"),
                "Lombok should be on classpath");
        
        // Test jqwik (property-based testing)
        assertDoesNotThrow(() -> Class.forName("net.jqwik.api.Property"),
                "jqwik should be on classpath");
    }
}
