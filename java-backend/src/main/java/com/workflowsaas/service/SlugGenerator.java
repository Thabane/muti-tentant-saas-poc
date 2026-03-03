package com.workflowsaas.service;

import com.workflowsaas.repository.TenantRepository;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating unique, URL-safe tenant slugs from organization names.
 * Slugs follow the pattern: ^[a-z0-9]+(?:-[a-z0-9]+)*$
 */
@Component
class SlugGenerator {

    private static final int MAX_UNIQUENESS_ATTEMPTS = 10;

    /**
     * Generates a unique, URL-safe slug from an organization name.
     *
     * @param organizationName the organization name to convert to a slug
     * @param repository the tenant repository for uniqueness checks
     * @return a unique slug
     * @throws IllegalArgumentException if organization name is null, blank, or contains no alphanumeric characters
     * @throws IllegalStateException if unable to generate unique slug after maximum attempts
     */
    String generateSlug(String organizationName, TenantRepository repository) {
        if (organizationName == null || organizationName.isBlank()) {
            throw new IllegalArgumentException("Organization name cannot be null or blank");
        }

        String baseSlug = sanitizeSlug(organizationName);

        if (baseSlug.isEmpty()) {
            throw new IllegalArgumentException("Organization name must contain at least one alphanumeric character");
        }

        return ensureUnique(baseSlug, repository);
    }

    /**
     * Sanitizes an input string to create a URL-safe slug.
     * - Converts to lowercase
     * - Replaces non-alphanumeric characters (except hyphens) with hyphens
     * - Collapses consecutive hyphens to single hyphen
     * - Removes leading and trailing hyphens
     *
     * @param input the input string to sanitize
     * @return the sanitized slug
     */
    private String sanitizeSlug(String input) {
        // Convert to lowercase
        String slug = input.toLowerCase();

        // Replace non-alphanumeric characters (except hyphens) with hyphens
        slug = slug.replaceAll("[^a-z0-9-]+", "-");

        // Collapse consecutive hyphens to single hyphen
        slug = slug.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        return slug;
    }

    /**
     * Ensures the slug is unique by appending numeric suffixes if needed.
     * Tries baseSlug, then baseSlug-2, baseSlug-3, etc.
     *
     * @param baseSlug the base slug to make unique
     * @param repository the tenant repository for uniqueness checks
     * @return a unique slug
     * @throws IllegalStateException if unable to generate unique slug after maximum attempts
     */
    private String ensureUnique(String baseSlug, TenantRepository repository) {
        String candidateSlug = baseSlug;

        for (int attempt = 1; attempt <= MAX_UNIQUENESS_ATTEMPTS; attempt++) {
            if (!repository.existsBySlug(candidateSlug)) {
                return candidateSlug;
            }

            // Append numeric suffix for next attempt
            candidateSlug = baseSlug + "-" + (attempt + 1);
        }

        throw new IllegalStateException(
            "Unable to generate unique slug after " + MAX_UNIQUENESS_ATTEMPTS + " attempts for base: " + baseSlug
        );
    }
}
