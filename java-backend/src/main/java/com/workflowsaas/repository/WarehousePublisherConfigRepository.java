package com.workflowsaas.repository;

import com.workflowsaas.entity.WarehousePublisherConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for WarehousePublisherConfig entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface WarehousePublisherConfigRepository extends JpaRepository<WarehousePublisherConfig, UUID> {
    
    /**
     * Find warehouse publisher configuration by publisher configuration ID and tenant ID.
     * Ensures tenant isolation by requiring both IDs.
     *
     * @param publisherConfigId the publisher configuration ID
     * @param tenantId the tenant ID
     * @return Optional containing the warehouse publisher configuration if found
     */
    Optional<WarehousePublisherConfig> findByPublisherConfigIdAndTenantId(UUID publisherConfigId, UUID tenantId);
}
