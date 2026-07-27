package org.nlh4j.saas.membershiphub.config;

/**
 * Thread‑local holder for the current tenant identifier.
 * <p>
 * The {@code TenantContext} is used throughout the application to propagate the tenant
 * information (extracted from the {@code X-Tenant-ID} HTTP header by a request filter) to
 * lower layers such as JPA repositories, Kafka producers, etc.
 * </p>
 *
 * <p>
 * The implementation is deliberately lightweight and does not depend on any Quarkus
 * specific APIs, allowing it to be used in both reactive and imperative code paths.
 * </p>
 *
 * <pre>
 * // Example usage in a service method
 * public void someMethod() {
 *     String tenantId = TenantContext.getTenantId();
 *     // use tenantId for tenant‑aware queries
 * }
 * </pre>
 *
 * @author  OpenAI ChatGPT
 */
public final class TenantContext {

    /** Header name used by the HTTP filter to pass the tenant identifier. */
    public static final String TENANT_HEADER = "X-Tenant-ID";

    /** Thread‑local storage for the tenant identifier. */
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    /** Private constructor to prevent instantiation. */
    private TenantContext() {
        // utility class
    }

    /**
     * Sets the tenant identifier for the current execution thread.
     *
     * @param tenantId the tenant identifier; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code tenantId} is {@code null} or blank
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID must not be null or blank");
        }
        TENANT_ID.set(tenantId);
    }

    /**
     * Retrieves the tenant identifier associated with the current execution thread.
     *
     * @return the tenant identifier, or {@code null} if none has been set
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Clears the tenant identifier from the current thread.
     * This method should be called at the end of a request to avoid leaking
     * tenant information to subsequent requests handled by the same thread.
     */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * Utility method to check whether a tenant identifier is currently bound.
     *
     * @return {@code true} if a tenant identifier is present, {@code false} otherwise
     */
    public static boolean isPresent() {
        return TENANT_ID.get() != null;
    }
}