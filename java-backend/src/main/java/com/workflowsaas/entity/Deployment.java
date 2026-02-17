package com.workflowsaas.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Deployment entity representing a workflow deployed to a specific environment.
 * Supports selective rollouts with percentage-based traffic routing.
 */
@Entity
@Table(name = "deployments")
@Data
public class Deployment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;
    
    @Column(nullable = false, length = 50)
    private String environment;
    
    @Column(name = "deployment_key")
    private String deploymentKey;
    
    @Column(length = 50)
    private String status = "pending";
    
    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage = 0;
    
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;
    
    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    @PrePersist
    protected void onCreate() {
        deployedAt = LocalDateTime.now();
    }
}
