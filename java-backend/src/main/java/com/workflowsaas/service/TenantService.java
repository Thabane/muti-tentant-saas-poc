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
import com.workflowsaas.security.JwtTokenProvider;
import com.workflowsaas.security.TenantContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for tenant management operations.
 */
@Service
public class TenantService {
    
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public TenantService(TenantRepository tenantRepository, 
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
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
        tenant.setPasswordHash(passwordEncoder.encode(request.password()));
        
        tenant = tenantRepository.save(tenant);
        
        String token = jwtTokenProvider.generateToken(tenant.getId(), tenant.getEmail());
        TenantResponse tenantResponse = toTenantResponse(tenant);
        
        return new AuthResponse(tenantResponse, token);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.password(), tenant.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        String token = jwtTokenProvider.generateToken(tenant.getId(), tenant.getEmail());
        TenantResponse tenantResponse = toTenantResponse(tenant);
        
        return new AuthResponse(tenantResponse, token);
    }
    
    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        
        return toTenantResponse(tenant);
    }
    
    @Transactional
    public void completeOnboarding() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Not authenticated");
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
