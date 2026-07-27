package org.nlh4j.saas.membershiphub.util;

import java.time.Instant;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.nlh4j.saas.membershiphub.config.TenantContext;

/**
 * Standard wrapper for all REST API responses.
 * <p>
 * The wrapper contains a success flag, a human readable message, the payload data,
 * the tenant identifier (extracted from {@link TenantContext}) and a timestamp.
 * It also provides convenient factory methods and a {@link #toResponse()} helper
 * that builds a JAX‑RS {@link Response} with the {@code X-Tenant-ID} header.
 * </p>
 *
 * @param <T> type of the payload data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "message", "data", "tenantId", "timestamp" })
public final class ResponseWrapper<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String tenantId;
    private final long timestamp;

    private ResponseWrapper(boolean success, String message, T data, String tenantId) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.tenantId = tenantId;
        this.timestamp = Instant.now().toEpochMilli();
    }

    /** @return true if the request succeeded */
    public boolean isSuccess() {
        return success;
    }

    /** @return human readable message */
    public String getMessage() {
        return message;
    }

    /** @return payload data */
    public T getData() {
        return data;
    }

    /** @return tenant identifier associated with the request */
    public String getTenantId() {
        return tenantId;
    }

    /** @return epoch‑millis when the response was created */
    public long getTimestamp() {
        return timestamp;
    }

    /* --------------------------------------------------------------------- *
     *  Factory methods
     * --------------------------------------------------------------------- */

    /**
     * Creates a successful wrapper without a custom message.
     *
     * @param data payload
     * @param <T>  payload type
     * @return wrapper instance
     */
    public static <T> ResponseWrapper<T> ok(T data) {
        return new ResponseWrapper<>(true, "OK", data, TenantContext.getTenantId());
    }

    /**
     * Creates a successful wrapper with a custom message.
     *
     * @param message custom success message
     * @param data    payload
     * @param <T>     payload type
     * @return wrapper instance
     */
    public static <T> ResponseWrapper<T> ok(String message, T data) {
        return new ResponseWrapper<>(true, message, data, TenantContext.getTenantId());
    }

    /**
     * Creates an error wrapper.
     *
     * @param message error description
     * @param <T>     payload type (null)
     * @return wrapper instance
     */
    public static <T> ResponseWrapper<T> error(String message) {
        return new ResponseWrapper<>(false, message, null, TenantContext.getTenantId());
    }

    /* --------------------------------------------------------------------- *
     *  JAX‑RS integration helpers
     * --------------------------------------------------------------------- */

    /**
     * Builds a {@link Response} object with the wrapper as the entity and
     * adds the {@code X-Tenant-ID} header.
     *
     * @param status HTTP status code
     * @return JAX‑RS {@link Response}
     */
    public Response toResponse(int status) {
        ResponseBuilder builder = Response.status(status).entity(this);
        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId);
        }
        // Preserve standard content‑type for JSON serialization
        builder.header(HttpHeaders.CONTENT_TYPE, "application/json");
        return builder.build();
    }

    /**
     * Shortcut for {@code toResponse(Response.Status.OK.getStatusCode())}.
     *
     * @return JAX‑RS {@link Response} with HTTP 200
     */
    public Response toOkResponse() {
        return toResponse(Response.Status.OK.getStatusCode());
    }

    /**
     * Shortcut for {@code toResponse(Response.Status.BAD_REQUEST.getStatusCode())}.
     *
     * @return JAX‑RS {@link Response} with HTTP 400
     */
    public Response toBadRequestResponse() {
        return toResponse(Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Shortcut for {@code toResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())}.
     *
     * @return JAX‑RS {@link Response} with HTTP 500
     */
    public Response toServerErrorResponse() {
        return toResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}