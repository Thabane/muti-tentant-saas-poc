package com.workflowsaas.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Tenant entity.
 * Validates entity structure, field mappings, and lifecycle callbacks.
 */
class TenantTest {

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
    }

    @Test
    void testTenantCreation() {
        assertNotNull(tenant, "Tenant instance should be created");
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        tenant.setId(id);
        assertEquals(id, tenant.getId(), "ID should match the set value");
    }

    @Test
    void testSetAndGetName() {
        String name = "Test Organization";
        tenant.setName(name);
        assertEquals(name, tenant.getName(), "Name should match the set value");
    }

    @Test
    void testSetAndGetSlug() {
        String slug = "test-org";
        tenant.setSlug(slug);
        assertEquals(slug, tenant.getSlug(), "Slug should match the set value");
    }

    @Test
    void testSetAndGetEmail() {
        String email = "test@example.com";
        tenant.setEmail(email);
        assertEquals(email, tenant.getEmail(), "Email should match the set value");
    }

    @Test
    void testSetAndGetPasswordHash() {
        String passwordHash = "$2a$10$abcdefghijklmnopqrstuv";
        tenant.setPasswordHash(passwordHash);
        assertEquals(passwordHash, tenant.getPasswordHash(), "Password hash should match the set value");
    }

    @Test
    void testFeaturesDefaultsToEmptyMap() {
        assertNotNull(tenant.getFeatures(), "Features should not be null");
        assertTrue(tenant.getFeatures().isEmpty(), "Features should be empty by default");
    }

    @Test
    void testSetAndGetFeatures() {
        Map<String, Object> features = new HashMap<>();
        features.put("feature1", true);
        features.put("feature2", "value");
        tenant.setFeatures(features);
        assertEquals(features, tenant.getFeatures(), "Features should match the set value");
        assertEquals(2, tenant.getFeatures().size(), "Features should contain 2 entries");
    }

    @Test
    void testOnboardingCompletedDefaultsToFalse() {
        assertNotNull(tenant.getOnboardingCompleted(), "Onboarding completed should not be null");
        assertFalse(tenant.getOnboardingCompleted(), "Onboarding completed should default to false");
    }

    @Test
    void testSetAndGetOnboardingCompleted() {
        tenant.setOnboardingCompleted(true);
        assertTrue(tenant.getOnboardingCompleted(), "Onboarding completed should be true");
    }

    @Test
    void testOnCreateSetsTimestamps() {
        // Simulate @PrePersist callback
        tenant.onCreate();
        
        assertNotNull(tenant.getCreatedAt(), "Created at should be set");
        assertNotNull(tenant.getUpdatedAt(), "Updated at should be set");
        assertEquals(tenant.getCreatedAt(), tenant.getUpdatedAt(), 
                "Created at and updated at should be equal on creation");
    }

    @Test
    void testOnUpdateModifiesUpdatedAt() throws InterruptedException {
        // Simulate @PrePersist callback
        tenant.onCreate();
        LocalDateTime originalCreatedAt = tenant.getCreatedAt();
        LocalDateTime originalUpdatedAt = tenant.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        // Simulate @PreUpdate callback
        tenant.onUpdate();
        
        assertEquals(originalCreatedAt, tenant.getCreatedAt(), 
                "Created at should not change on update");
        assertTrue(tenant.getUpdatedAt().isAfter(originalUpdatedAt), 
                "Updated at should be after the original timestamp");
    }

    @Test
    void testFullTenantPopulation() {
        UUID id = UUID.randomUUID();
        String name = "Acme Corp";
        String slug = "acme-corp";
        String email = "admin@acme.com";
        String passwordHash = "$2a$10$hashedpassword";
        Map<String, Object> features = new HashMap<>();
        features.put("premium", true);
        
        tenant.setId(id);
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setEmail(email);
        tenant.setPasswordHash(passwordHash);
        tenant.setFeatures(features);
        tenant.setOnboardingCompleted(true);
        tenant.onCreate();
        
        assertEquals(id, tenant.getId());
        assertEquals(name, tenant.getName());
        assertEquals(slug, tenant.getSlug());
        assertEquals(email, tenant.getEmail());
        assertEquals(passwordHash, tenant.getPasswordHash());
        assertEquals(features, tenant.getFeatures());
        assertTrue(tenant.getOnboardingCompleted());
        assertNotNull(tenant.getCreatedAt());
        assertNotNull(tenant.getUpdatedAt());
    }

    @Test
    void testTenantEqualsAndHashCode() {
        Tenant tenant1 = new Tenant();
        Tenant tenant2 = new Tenant();
        
        UUID id = UUID.randomUUID();
        tenant1.setId(id);
        tenant1.setName("Test");
        tenant1.setEmail("test@example.com");
        
        tenant2.setId(id);
        tenant2.setName("Test");
        tenant2.setEmail("test@example.com");
        
        // Lombok @Data generates equals and hashCode
        assertEquals(tenant1, tenant2, "Tenants with same data should be equal");
        assertEquals(tenant1.hashCode(), tenant2.hashCode(), "Hash codes should match");
    }
}
