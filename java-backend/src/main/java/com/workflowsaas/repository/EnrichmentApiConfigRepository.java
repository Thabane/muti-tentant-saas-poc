package com.workflowsaas.repository;

import com.workflowsaas.entity.EnrichmentApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for EnrichmentApiConfig entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface EnrichmentApiConfigRepository extends JpaRepository<EnrichmentApiConfig, UUID> {
    
    /**
     * Find all enrichment API configurations by app configuration ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param appConfigId the app configuration ID
     * @param tenantId the tenant ID
     * @return List of enrichment API configurations
     */
    List<EnrichmentApiConfig> findByAppConfigurationIdAndTenantId(UUID appConfigId, UUID tenantId);
    
    /**
     * Delete enrichment API configuration by ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param id the enrichment API configuration ID
     * @param tenantId the tenant ID
     */
    void deleteByIdAndTenantId(UUID id, UUID tenantId);
}
