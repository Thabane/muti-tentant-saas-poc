package com.workflowsaas.repository;

import com.workflowsaas.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tenant entity operations.
 * Provides methods for tenant authentication and uniqueness validation.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    Optional<Tenant> findByEmail(String email);
    
    Optional<Tenant> findBySlug(String slug);
    
    boolean existsByEmail(String email);
    
    boolean existsBySlug(String slug);
}
