package com.workflowsaas.service;

/**
 * Service for generating and validating API keys.
 */
public interface ApiKeyService {
    
    /**
     * Generates a cryptographically secure API key.
     * Format: app_[32 random alphanumeric characters]
     * 
     * @return generated API key
     */
    String generateApiKey();
    
    /**
     * Hashes API key for secure storage using BCrypt.
     * 
     * @param apiKey the API key to hash
     * @return hashed API key
     */
    String hashApiKey(String apiKey);
    
    /**
     * Validates API key against stored hash.
     * 
     * @param apiKey the API key to validate
     * @param hash the stored hash
     * @return true if valid, false otherwise
     */
    boolean validateApiKey(String apiKey, String hash);
}
