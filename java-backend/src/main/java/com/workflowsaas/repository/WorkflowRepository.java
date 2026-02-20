package com.workflowsaas.repository;

import com.workflowsaas.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Workflow entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    
    List<Workflow> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    
    Optional<Workflow> findByIdAndTenantId(UUID id, UUID tenantId);
    
    boolean existsByTenantIdAndName(UUID tenantId, String name);
    
    boolean existsByApiPath(String apiPath);
    
    List<Workflow> findByAppIdOrderByCreatedAtDesc(UUID appId);
}
