package com.workflowsaas.security;

import java.util.UUID;

/**
 * Thread-local storage for tenant context.
 * Stores the authenticated tenant ID for the current request.
 */
public class TenantContextHolder {
    
    private static final ThreadLocal<UUID> tenantContext = new ThreadLocal<>();
    
    public static void setTenantId(UUID tenantId) {
        tenantContext.set(tenantId);
    }
    
    public static UUID getTenantId() {
        return tenantContext.get();
    }
    
    public static void clear() {
        tenantContext.remove();
    }
}
