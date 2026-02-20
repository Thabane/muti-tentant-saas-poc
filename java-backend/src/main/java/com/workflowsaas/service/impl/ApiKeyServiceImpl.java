package com.workflowsaas.service.impl;

import com.workflowsaas.service.ApiKeyService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementation of ApiKeyService for generating and validating API keys.
 * Note: Using SHA-256 for hashing since Spring Security is disabled.
 */
@Service
public class ApiKeyServiceImpl implements ApiKeyService {
    
    private static final String API_KEY_PREFIX = "app_";
    private static final int API_KEY_LENGTH = 32;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public String generateApiKey() {
        StringBuilder apiKey = new StringBuilder(API_KEY_PREFIX);
        
        for (int i = 0; i < API_KEY_LENGTH; i++) {
            int index = secureRandom.nextInt(ALPHANUMERIC.length());
            apiKey.append(ALPHANUMERIC.charAt(index));
        }
        
        return apiKey.toString();
    }
    
    @Override
    public String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    @Override
    public boolean validateApiKey(String apiKey, String hash) {
        String computedHash = hashApiKey(apiKey);
        return computedHash.equals(hash);
    }
}
