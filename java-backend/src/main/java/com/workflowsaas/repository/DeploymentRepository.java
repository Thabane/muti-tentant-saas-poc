package com.workflowsaas.repository;

import com.workflowsaas.entity.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Deployment entity operations.
 * All queries are tenant-scoped for data isolation.
 */
@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {
    
    List<Deployment> findByTenantIdOrderByDeployedAtDesc(UUID tenantId);
    
    List<Deployment> findByTenantIdAndEnvironmentOrderByDeployedAtDesc(UUID tenantId, String environment);
    
    Optional<Deployment> findByIdAndTenantId(UUID id, UUID tenantId);
}
