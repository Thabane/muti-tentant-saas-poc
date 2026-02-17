package com.workflowsaas.controller;

import com.workflowsaas.dto.request.LoginRequest;
import com.workflowsaas.dto.request.RegisterRequest;
import com.workflowsaas.dto.response.AuthResponse;
import com.workflowsaas.dto.response.TenantResponse;
import com.workflowsaas.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for tenant management endpoints.
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    
    private final TenantService tenantService;
    
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = tenantService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = tenantService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<TenantResponse> getCurrentTenant() {
        TenantResponse response = tenantService.getCurrentTenant();
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/onboarding")
    public ResponseEntity<Map<String, Boolean>> completeOnboarding() {
        tenantService.completeOnboarding();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
