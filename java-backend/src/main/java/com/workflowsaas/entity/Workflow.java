package com.workflowsaas.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Workflow entity representing a BPMN or DMN process definition.
 * Workflows are versioned and tenant-scoped.
 */
@Entity
@Table(name = "workflows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name", "version"}))
@Data
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(name = "bpmn_xml", columnDefinition = "TEXT")
    private String bpmnXml;
    
    @Column(name = "dmn_xml", columnDefinition = "TEXT")
    private String dmnXml;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column(length = 50)
    private String status = "draft";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
