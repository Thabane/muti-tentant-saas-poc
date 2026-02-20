package com.workflowsaas;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that SECURITY_DISABLED_SUMMARY.md documentation is accurate.
 * This test verifies that the security components mentioned in the documentation
 * have actually been removed from the codebase.
 */
class DocumentationValidationTest {

    private static final String SECURITY_DIR = "src/main/java/com/workflowsaas/security";
    private static final String CONFIG_DIR = "src/main/java/com/workflowsaas/config";

    @Test
    void testSecurityConfigDeleted() {
        Path securityConfigPath = Paths.get(CONFIG_DIR, "SecurityConfig.java");
        assertFalse(Files.exists(securityConfigPath),
                "SecurityConfig.java should be deleted according to documentation");
    }

    @Test
    void testJwtTokenProviderDeleted() {
        Path jwtProviderPath = Paths.get(SECURITY_DIR, "JwtTokenProvider.java");
        assertFalse(Files.exists(jwtProviderPath),
                "JwtTokenProvider.java should be deleted according to documentation");
    }

    @Test
    void testJwtAuthenticationFilterDeleted() {
        Path jwtFilterPath = Paths.get(SECURITY_DIR, "JwtAuthenticationFilter.java");
        assertFalse(Files.exists(jwtFilterPath),
                "JwtAuthenticationFilter.java should be deleted according to documentation");
    }

    @Test
    void testApiKeyAuthenticationFilterDeleted() {
        Path apiKeyFilterPath = Paths.get(SECURITY_DIR, "ApiKeyAuthenticationFilter.java");
        assertFalse(Files.exists(apiKeyFilterPath),
                "ApiKeyAuthenticationFilter.java should be deleted according to documentation");
    }

    @Test
    void testTenantContextHolderStillExists() {
        Path tenantContextPath = Paths.get(SECURITY_DIR, "TenantContextHolder.java");
        assertTrue(Files.exists(tenantContextPath),
                "TenantContextHolder.java should still exist according to documentation");
    }

    @Test
    void testPomXmlNoSpringSecurityDependency() throws Exception {
        Path pomPath = Paths.get("pom.xml");
        assertTrue(Files.exists(pomPath), "pom.xml should exist");

        String pomContent = Files.readString(pomPath);
        
        // Remove all XML comments (including multi-line) before checking
        String contentWithoutComments = pomContent.replaceAll("(?s)<!--.*?-->", "");
        
        assertFalse(contentWithoutComments.contains("spring-boot-starter-security"),
                "pom.xml should not contain uncommented spring-boot-starter-security dependency");
        assertFalse(contentWithoutComments.contains("spring-security-test"),
                "pom.xml should not contain uncommented spring-security-test dependency");
    }

    @Test
    void testApiKeyServiceImplExists() {
        Path apiKeyServicePath = Paths.get("src/main/java/com/workflowsaas/service/impl/ApiKeyServiceImpl.java");
        assertTrue(Files.exists(apiKeyServicePath),
                "ApiKeyServiceImpl.java should exist");
    }

    @Test
    void testTenantServiceExists() {
        Path tenantServicePath = Paths.get("src/main/java/com/workflowsaas/service/TenantService.java");
        assertTrue(Files.exists(tenantServicePath),
                "TenantService.java should exist");
    }

    @Test
    void testSecurityDirectoryStructure() {
        File securityDir = new File(SECURITY_DIR);
        if (securityDir.exists() && securityDir.isDirectory()) {
            File[] files = securityDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertNotNull(files, "Security directory should be readable");
            
            // Should only contain TenantContextHolder.java
            assertEquals(1, files.length,
                    "Security directory should only contain TenantContextHolder.java");
            assertEquals("TenantContextHolder.java", files[0].getName(),
                    "The only file in security directory should be TenantContextHolder.java");
        }
    }
}
