package com.workflowsaas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the main Application.
 * Verifies that the Spring Boot application context loads successfully.
 * 
 * Note: Excludes Spring Security auto-configuration since security is disabled.
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration"
})
class ApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
    }

    @Test
    void applicationClassExists() {
        assertDoesNotThrow(() -> Class.forName("com.workflowsaas.Application"),
                "Application class should exist");
    }

    @Test
    void mainMethodExists() throws NoSuchMethodException {
        var mainMethod = Application.class.getMethod("main", String[].class);
        assertNotNull(mainMethod, "Main method should exist");
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                "Main method should be static");
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                "Main method should be public");
    }

    @Test
    void applicationHasSpringBootApplicationAnnotation() {
        assertTrue(Application.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "Application class should have @SpringBootApplication annotation");
    }
}
