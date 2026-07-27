package org.nlh4j.saas.membershiphub.util;

import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Utility class to standardize API responses across the Membership‑Hub backend.
 * <p>
 * The wrapper contains a uniform payload structure and automatically injects the
 * current {@code tenantId} (retrieved from {@link TenantContext}) into the
 * response body as well as the {@code X‑Tenant‑ID} HTTP header.
 * </p>
 *
 * @param <T> type of the data payload
 */
@ApplicationScoped
public class ResponseWrapper<T> {

    /** HTTP status code (e.g. 200, 400, 500). */
    private final int status;

    /** Human‑readable message describing the result. */
    private final String message;

    /** Payload data (may be {@code null}). */
    @JsonInclude(Include.NON_NULL)
    private final T data;

    /** Timestamp of the response generation (epoch millis). */
    private final long timestamp;

    /** Tenant identifier resolved from the request context. */
    private final String tenantId;

    @Inject
    private TenantContext tenantContext;

    private ResponseWrapper(int status, String message, T data, String tenantId) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
        this.tenantId = tenantId;
    }

    /** Factory method for a successful response (HTTP 200). */
    public static <T> ResponseWrapper<T> success(T data) {
        String tenant = TenantContextHolder.getTenantId();
        return new ResponseWrapper<>(Response.Status.OK.getStatusCode(), "OK", data, tenant);
    }

    /** Factory method for a successful response with a custom message. */
    public static <T> ResponseWrapper<T> success(String message, T data) {
        String tenant = TenantContextHolder.getTenantId();
        return new ResponseWrapper<>(Response.Status.OK.getStatusCode(), message, data, tenant);
    }

    /** Factory method for an error response. */
    public static <T> ResponseWrapper<T> error(int status, String message) {
        String tenant = TenantContextHolder.getTenantId();
        return new ResponseWrapper<>(status, message, null, tenant);
    }

    /** Builds a {@link jakarta.ws.rs.core.Response} object ready to be returned from a JAX‑RS resource. */
    public Response toResponse() {
        ResponseBuilder builder = Response.status(this.status)
                .entity(this)
                .type(MediaType.APPLICATION_JSON);

        if (this.tenantId != null) {
            builder.header("X-Tenant-ID", this.tenantId);
        }
        return builder.build();
    }

    // -----------------------------------------------------------------------
    // Getters (required for JSON serialization)
    // -----------------------------------------------------------------------
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTenantId() {
        return tenantId;
    }

    // -----------------------------------------------------------------------
    // Helper to obtain tenant id without needing CDI injection (static context)
    // -----------------------------------------------------------------------
    private static final class TenantContextHolder {
        private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

        static void setTenantId(String id) {
            TENANT.set(id);
        }

        static String getTenantId() {
            return TENANT.get();
        }
    }

    /**
     * Simple thread‑local holder for the current tenant identifier.
     * This class is deliberately lightweight to avoid any heavy framework
     * dependencies inside the wrapper. It should be populated by a request
     * filter (e.g., {@code TenantFilter}) early in the request lifecycle.
     */
    public static final class TenantContext {

        /** Retrieves the tenant identifier for the current thread. */
        public String getCurrentTenantId() {
            return TenantContextHolder.getTenantId();
        }

        /** Sets the tenant identifier for the current thread. */
        public void setCurrentTenantId(String tenantId) {
            TenantContextHolder.setTenantId(tenantId);
        }
    }
}