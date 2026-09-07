// [ARC-000] [REQ-012] Attendance Service Application Main Entry Point
/**
 * @traceability [ARC-000], [REQ-012]
 * Enterprise-grade Quarkus main entry point class for attendance-service.
 * Configures production runtime parameters: disables startup banner, binds explicit HTTP port,
 * and initializes SmallRye Health management extensions.
 */
package org.nlh4j.membershiphub.attendanceservice;

// Import Quarkus runtime framework and main application startup annotations
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Main application runner annotated with @QuarkusMain to bootstrap the Quarkus microservice container.
 */
@QuarkusMain
public class AttendanceServiceApplication {

    /**
     * Top-of-class immutable configuration constants for runtime system properties [0.2].
     */
    public static final String PROP_QUARKUS_BANNER_ENABLED = "quarkus.banner.enabled";
    public static final String PROP_QUARKUS_HTTP_PORT = "quarkus.http.port";
    public static final String PROP_QUARKUS_HEALTH_UI_PATH = "quarkus.smallrye.health.ui.path";
    
    public static final String VALUE_BANNER_DISABLED = "false";
    public static final String VALUE_HTTP_PORT = "8080";
    public static final String VALUE_HEALTH_UI_PATH = "/q/health-ui";

    /**
     * Main execution entry point for the attendance-service microservice.
     * 
     * @param args Command-line arguments passed during container startup.
     */
    public static void main(String[] args) {
        // [PROCESS] [ARC-000] Initializing runtime system property configurations for container hardening
        System.setProperty(PROP_QUARKUS_BANNER_ENABLED, VALUE_BANNER_DISABLED);
        System.setProperty(PROP_QUARKUS_HTTP_PORT, VALUE_HTTP_PORT);
        
        // [PROCESS] [REQ-012] Configuring SmallRye health check endpoints for Kubernetes liveness/readiness probes
        System.setProperty(PROP_QUARKUS_HEALTH_UI_PATH, VALUE_HEALTH_UI_PATH);
        
        // [PROCESS] [ARC-000] Launching the Quarkus reactive runtime container engine
        Quarkus.run(AttendanceServiceApplication.class, args);
    }
}