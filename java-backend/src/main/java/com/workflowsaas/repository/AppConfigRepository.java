package com.workflowsaas.repository;

import com.workflowsaas.entity.AppConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AppConfiguration entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface AppConfigRepository extends JpaRepository<AppConfiguration, UUID> {
    
    /**
     * Find app configuration by app ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param appId the app ID
     * @param tenantId the tenant ID
     * @return Optional containing the configuration if found
     */
    Optional<AppConfiguration> findByAppIdAndTenantId(UUID appId, UUID tenantId);
    
    /**
     * Delete app configuration by app ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param appId the app ID
     * @param tenantId the tenant ID
     */
    void deleteByAppIdAndTenantId(UUID appId, UUID tenantId);
}
