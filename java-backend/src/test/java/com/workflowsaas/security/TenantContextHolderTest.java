package com.workflowsaas.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TenantContextHolder.
 * Validates thread-local storage behavior and tenant context isolation.
 */
class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        // Clean up after each test to prevent context leakage
        TenantContextHolder.clear();
    }

    @Test
    void testSetAndGetTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        
        assertEquals(tenantId, TenantContextHolder.getTenantId(), 
                "Retrieved tenant ID should match the set value");
    }

    @Test
    void testGetTenantIdReturnsNullWhenNotSet() {
        assertNull(TenantContextHolder.getTenantId(), 
                "Tenant ID should be null when not set");
    }

    @Test
    void testClearRemovesTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        
        assertNotNull(TenantContextHolder.getTenantId(), 
                "Tenant ID should be set");
        
        TenantContextHolder.clear();
        
        assertNull(TenantContextHolder.getTenantId(), 
                "Tenant ID should be null after clear");
    }

    @Test
    void testMultipleSetOperationsOverwritePreviousValue() {
        UUID firstTenantId = UUID.randomUUID();
        UUID secondTenantId = UUID.randomUUID();
        
        TenantContextHolder.setTenantId(firstTenantId);
        assertEquals(firstTenantId, TenantContextHolder.getTenantId());
        
        TenantContextHolder.setTenantId(secondTenantId);
        assertEquals(secondTenantId, TenantContextHolder.getTenantId(), 
                "Second set should overwrite first value");
    }

    @Test
    void testThreadIsolation() throws InterruptedException {
        UUID mainThreadTenantId = UUID.randomUUID();
        UUID otherThreadTenantId = UUID.randomUUID();
        
        // Set tenant ID in main thread
        TenantContextHolder.setTenantId(mainThreadTenantId);
        
        AtomicReference<UUID> retrievedInOtherThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create another thread and set different tenant ID
        Thread otherThread = new Thread(() -> {
            TenantContextHolder.setTenantId(otherThreadTenantId);
            retrievedInOtherThread.set(TenantContextHolder.getTenantId());
            latch.countDown();
        });
        
        otherThread.start();
        latch.await();
        
        // Verify main thread still has its own tenant ID
        assertEquals(mainThreadTenantId, TenantContextHolder.getTenantId(), 
                "Main thread should retain its tenant ID");
        
        // Verify other thread had its own tenant ID
        assertEquals(otherThreadTenantId, retrievedInOtherThread.get(), 
                "Other thread should have its own tenant ID");
    }

    @Test
    void testClearDoesNotAffectOtherThreads() throws InterruptedException {
        UUID mainThreadTenantId = UUID.randomUUID();
        UUID otherThreadTenantId = UUID.randomUUID();
        
        TenantContextHolder.setTenantId(mainThreadTenantId);
        
        AtomicReference<UUID> retrievedAfterMainClear = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Thread otherThread = new Thread(() -> {
            TenantContextHolder.setTenantId(otherThreadTenantId);
            
            // Wait a bit to ensure main thread clears
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            retrievedAfterMainClear.set(TenantContextHolder.getTenantId());
            latch.countDown();
        });
        
        otherThread.start();
        Thread.sleep(10);
        
        // Clear in main thread
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId(), 
                "Main thread should have null after clear");
        
        latch.await();
        
        // Other thread should still have its value
        assertEquals(otherThreadTenantId, retrievedAfterMainClear.get(), 
                "Other thread should not be affected by main thread clear");
    }

    @Test
    void testSetNullTenantId() {
        TenantContextHolder.setTenantId(null);
        assertNull(TenantContextHolder.getTenantId(), 
                "Should be able to set null tenant ID");
    }

    @Test
    void testMultipleClearOperationsAreSafe() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
        
        // Multiple clears should not throw exception
        assertDoesNotThrow(() -> TenantContextHolder.clear(), 
                "Multiple clear operations should be safe");
        
        assertNull(TenantContextHolder.getTenantId());
    }
}
