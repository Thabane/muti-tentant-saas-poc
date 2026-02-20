package com.workflowsaas.service;

import com.workflowsaas.dto.request.LoginRequest;
import com.workflowsaas.dto.request.RegisterRequest;
import com.workflowsaas.dto.response.AuthResponse;
import com.workflowsaas.dto.response.TenantResponse;
import com.workflowsaas.entity.Tenant;
import com.workflowsaas.exception.BadRequestException;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.exception.UnauthorizedException;
import com.workflowsaas.repository.TenantRepository;
import com.workflowsaas.security.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantService.
 * Tests tenant registration, login, and onboarding with mocked dependencies.
 * 
 * Note: These tests reflect the current state where security is disabled
 * and passwords are stored in plain text. The service now uses a default
 * tenant ID when no tenant context is available.
 */
@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private UUID testTenantId;
    private UUID defaultTenantId;
    private Tenant testTenant;
    private Tenant defaultTenant;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        now = LocalDateTime.now();
        
        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setName("Test Tenant");
        testTenant.setSlug("test-tenant");
        testTenant.setEmail("test@example.com");
        testTenant.setPasswordHash("plaintext123"); // Plain text - security disabled
        testTenant.setOnboardingCompleted(false);
        testTenant.setCreatedAt(now);
        testTenant.setUpdatedAt(now);
        
        defaultTenant = new Tenant();
        defaultTenant.setId(defaultTenantId);
        defaultTenant.setName("Default Tenant");
        defaultTenant.setSlug("default");
        defaultTenant.setEmail("default@example.com");
        defaultTenant.setPasswordHash("default");
        defaultTenant.setOnboardingCompleted(false);
        defaultTenant.setCreatedAt(now);
        defaultTenant.setUpdatedAt(now);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "New Tenant",
            "new-tenant",
            "new@example.com",
            "password123"
        );
        
        when(tenantRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.existsBySlug(request.slug())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId(testTenantId);
            tenant.setCreatedAt(now);
            tenant.setUpdatedAt(now);
            return tenant;
        });

        // Act
        AuthResponse response = tenantService.register(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.tenant());
        assertEquals("New Tenant", response.tenant().name());
        assertEquals("new-tenant", response.tenant().slug());
        assertEquals("new@example.com", response.tenant().email());
        assertEquals(testTenantId.toString(), response.token());
        
        // Verify tenant context was set
        assertEquals(testTenantId, TenantContextHolder.getTenantId());
        
        verify(tenantRepository).existsByEmail(request.email());
        verify(tenantRepository).existsBySlug(request.slug());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "New Tenant",
            "new-tenant",
            "existing@example.com",
            "password123"
        );
        
        when(tenantRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> tenantService.register(request)
        );
        
        assertEquals("Email already exists", exception.getMessage());
        verify(tenantRepository).existsByEmail(request.email());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "plaintext123");
        
        when(tenantRepository.findByEmail(request.email())).thenReturn(Optional.of(testTenant));

        // Act
        AuthResponse response = tenantService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.tenant());
        assertEquals(testTenant.getName(), response.tenant().name());
        assertEquals(testTenant.getEmail(), response.tenant().email());
        assertEquals(testTenantId.toString(), response.token());
        
        // Verify tenant context was set
        assertEquals(testTenantId, TenantContextHolder.getTenantId());
        
        verify(tenantRepository).findByEmail(request.email());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        
        when(tenantRepository.findByEmail(request.email())).thenReturn(Optional.of(testTenant));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> tenantService.login(request)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(tenantRepository).findByEmail(request.email());
    }

    @Test
    void testGetCurrentTenant_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));

        // Act
        TenantResponse response = tenantService.getCurrentTenant();

        // Assert
        assertNotNull(response);
        assertEquals(testTenantId, response.id());
        assertEquals(testTenant.getName(), response.name());
        assertEquals(testTenant.getEmail(), response.email());
        
        verify(tenantRepository).findById(testTenantId);
    }

    @Test
    void testGetCurrentTenant_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear(); // No tenant context
        when(tenantRepository.findById(defaultTenantId)).thenReturn(Optional.of(defaultTenant));

        // Act
        TenantResponse response = tenantService.getCurrentTenant();

        // Assert
        assertNotNull(response);
        assertEquals(defaultTenantId, response.id());
        assertEquals("Default Tenant", response.name());
        
        // Verify it used the default tenant ID
        verify(tenantRepository).findById(defaultTenantId);
        verify(tenantRepository, never()).findById(testTenantId);
    }

    @Test
    void testCompleteOnboarding_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tenantService.completeOnboarding();

        // Assert
        assertTrue(testTenant.getOnboardingCompleted());
        verify(tenantRepository).findById(testTenantId);
        verify(tenantRepository).save(testTenant);
    }

    @Test
    void testCompleteOnboarding_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear(); // No tenant context
        when(tenantRepository.findById(defaultTenantId)).thenReturn(Optional.of(defaultTenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tenantService.completeOnboarding();

        // Assert
        assertTrue(defaultTenant.getOnboardingCompleted());
        
        // Verify it used the default tenant ID
        verify(tenantRepository).findById(defaultTenantId);
        verify(tenantRepository).save(defaultTenant);
        verify(tenantRepository, never()).findById(testTenantId);
    }

    @Test
    void testCompleteOnboarding_TenantNotFound() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> tenantService.completeOnboarding()
        );
        
        assertEquals("Tenant not found", exception.getMessage());
        verify(tenantRepository).findById(testTenantId);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testRegister_StoresPlainTextPassword() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "New Tenant",
            "new-tenant",
            "new@example.com",
            "mypassword"
        );
        
        when(tenantRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.existsBySlug(request.slug())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            // Verify password is stored as plain text (security disabled)
            assertEquals("mypassword", tenant.getPasswordHash());
            tenant.setId(testTenantId);
            return tenant;
        });

        // Act
        tenantService.register(request);

        // Assert
        verify(tenantRepository).save(argThat(tenant -> 
            "mypassword".equals(tenant.getPasswordHash())
        ));
    }

    @Test
    void testGetCurrentTenant_DefaultTenantNotFound() {
        // Arrange
        TenantContextHolder.clear(); // No tenant context
        when(tenantRepository.findById(defaultTenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> tenantService.getCurrentTenant()
        );
        
        assertEquals("Tenant not found", exception.getMessage());
        verify(tenantRepository).findById(defaultTenantId);
    }
}
