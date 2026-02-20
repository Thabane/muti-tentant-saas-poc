package com.workflowsaas.repository;

import com.workflowsaas.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for App entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface AppRepository extends JpaRepository<App, UUID> {
    
    List<App> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    
    Optional<App> findByIdAndTenantId(UUID id, UUID tenantId);
    
    Optional<App> findByApiKeyHash(String apiKeyHash);
}
