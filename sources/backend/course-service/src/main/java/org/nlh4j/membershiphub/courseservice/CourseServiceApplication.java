package org.nlh4j.membershiphub.courseservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Course Service Application entry point for the Membership Hub platform.
 * <p>
 * This class serves as the main bootstrap for the Quarkus-based course-service microservice,
 * providing core runtime initialization, health checks, and graceful shutdown handling.
 * </p>
 *
 * @traceability [ARC-000]
 */
@QuarkusMain
public class CourseServiceApplication {

    /**
     * Application entry point.
     * <p>
     * Launches the Quarkus runtime, which starts all registered extensions,
     * REST resources, background jobs, and reactive messaging connectors.
     * </p>
     *
     * @param args Command-line arguments passed to the application (currently unused).
     */
    public static void main(final String[] args) {
        // Bootstrap the Quarkus framework. This call blocks until the application shuts down.
        Quarkus.run(args);
    }
}