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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for tenant management operations.
 * Note: Security is disabled - passwords are stored in plain text.
 */
@Service
public class TenantService {
    
    private final TenantRepository tenantRepository;
    
    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (tenantRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new BadRequestException("Slug already exists");
        }
        
        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setSlug(request.slug());
        tenant.setEmail(request.email());
        tenant.setPasswordHash(request.password()); // Plain text - security disabled
        
        tenant = tenantRepository.save(tenant);
        
        // Set tenant context for this session
        TenantContextHolder.setTenantId(tenant.getId());
        
        TenantResponse tenantResponse = toTenantResponse(tenant);
        
        return new AuthResponse(tenantResponse, tenant.getId().toString());
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!request.password().equals(tenant.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        // Set tenant context for this session
        TenantContextHolder.setTenantId(tenant.getId());
        
        TenantResponse tenantResponse = toTenantResponse(tenant);
        
        return new AuthResponse(tenantResponse, tenant.getId().toString());
    }
    
    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            // TODO: When security is re-enabled, this should throw UnauthorizedException
            // For now, use default tenant ID since security is disabled
            tenantId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        
        return toTenantResponse(tenant);
    }
    
    @Transactional
    public void completeOnboarding() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            // TODO: When security is re-enabled, this should throw UnauthorizedException
            // For now, use default tenant ID since security is disabled
            tenantId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        
        tenant.setOnboardingCompleted(true);
        tenantRepository.save(tenant);
    }
    
    private TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getEmail(),
            tenant.getFeatures(),
            tenant.getOnboardingCompleted()
        );
    }
}
