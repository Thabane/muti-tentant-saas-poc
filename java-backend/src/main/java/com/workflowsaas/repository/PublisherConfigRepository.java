package com.workflowsaas.repository;

import com.workflowsaas.entity.PublisherConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PublisherConfig entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface PublisherConfigRepository extends JpaRepository<PublisherConfig, UUID> {
    
    /**
     * Find publisher configuration by app configuration ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param appConfigurationId the app configuration ID
     * @param tenantId the tenant ID
     * @return Optional containing the publisher configuration if found
     */
    Optional<PublisherConfig> findByAppConfigurationIdAndTenantId(UUID appConfigurationId, UUID tenantId);
    
    /**
     * Delete publisher configuration by app configuration ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param appConfigurationId the app configuration ID
     * @param tenantId the tenant ID
     */
    void deleteByAppConfigurationIdAndTenantId(UUID appConfigurationId, UUID tenantId);
}
