package org.nlh4j.membershiphub.userservice.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enterprise Resource Server and Security Identity Augmentor Configuration.
 * <p>
 * This configuration class registers the application root REST path prefix, sets up
 * dynamic augmentations for caller security identities extracted from incoming JWT claims,
 * and normalizes role claims into Quarkus runtime security authorization contexts.
 * </p>
 *
 * @traceability [ARC-006], [NFR-003]
 */
@ApplicationScoped
@ApplicationPath(ResourceServerConfig.API_V1_PATH_PREFIX)
public class ResourceServerConfig extends Application implements SecurityIdentityAugmentor {

    // [0.2] Top-of-Class Immutable Constants Declaration
    public static final String API_V1_PATH_PREFIX = "/api/v1";
    public static final String CLAIM_GROUPS = "groups";
    public static final String CLAIM_GROUP = "group";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ROLE = "role";
    public static final String DEFAULT_ISSUER = "membership-hub";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final int AUGMENTATION_PRIORITY_ORDER = 10;

    // Standard Logging Framework Instance
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerConfig.class);

    /**
     * Retrieves the augmentation priority order for chain processing.
     *
     * @return priority integer order
     */
    @Override
    public int priority() {
        // [ARC-006] Priority ordering for security identity augmentors in the filter chain
        return AUGMENTATION_PRIORITY_ORDER;
    }

    /**
     * Augments incoming reactive security identities with standardized application roles
     * extracted from JWT principal claims.
     *
     * @param identity caller's established identity
     * @param context  authentication context providing async execution facility
     * @return Uni containing augmented or unmodified SecurityIdentity
     */
    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        // [0.3] Entry-point logging with context tracking
        LOGGER.debug("[ENTRY] [ARC-006] Augmenting security identity for principal: {}",
                identity.isAnonymous() ? "ANONYMOUS" : identity.getPrincipal().getName());

        // Anonymous callers require no role elevation or claim mapping
        if (identity.isAnonymous()) {
            LOGGER.debug("[EXIT] [ARC-006] Security identity is anonymous; skipping claim augmentation.");
            return Uni.createFrom().item(identity);
        }

        return context.runBlocking(() -> {
            try {
                Principal principal = identity.getPrincipal();

                // Validate if caller principal originates from a MicroProfile / SmallRye JWT token
                if (principal instanceof JsonWebToken jwtPrincipal) {
                    LOGGER.debug("[PROCESS] [NFR-003] Extracting role and group claims from JWT Principal: {}",
                            jwtPrincipal.getName());

                    Set<String> resolvedRoles = extractRolesFromJwt(jwtPrincipal);

                    // Build hardened QuarkusSecurityIdentity containing standard + normalized roles
                    QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
                    for (String role : resolvedRoles) {
                        builder.addRole(role);
                        // Also inject non-prefixed role variant for strict @RolesAllowed compatibility
                        if (role.startsWith(ROLE_PREFIX)) {
                            builder.addRole(role.substring(ROLE_PREFIX.length()));
                        } else {
                            builder.addRole(ROLE_PREFIX + role);
                        }
                    }

                    SecurityIdentity augmentedIdentity = builder.build();
                    LOGGER.info("[EXIT] [ARC-006] Successfully augmented SecurityIdentity for user: {} with roles: {}",
                            augmentedIdentity.getPrincipal().getName(), augmentedIdentity.getRoles());
                    return augmentedIdentity;
                } else if (principal instanceof JWTCallerPrincipal callerPrincipal) {
                    LOGGER.debug("[PROCESS] [NFR-003] Extracting role claims from JWTCallerPrincipal: {}",
                            callerPrincipal.getName());

                    Set<String> resolvedRoles = extractRolesFromCallerPrincipal(callerPrincipal);
                    QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
                    for (String role : resolvedRoles) {
                        builder.addRole(role);
                        if (role.startsWith(ROLE_PREFIX)) {
                            builder.addRole(role.substring(ROLE_PREFIX.length()));
                        } else {
                            builder.addRole(ROLE_PREFIX + role);
                        }
                    }

                    SecurityIdentity augmentedIdentity = builder.build();
                    LOGGER.info("[EXIT] [ARC-006] Successfully augmented SecurityIdentity via caller principal for user: {}",
                            augmentedIdentity.getPrincipal().getName());
                    return augmentedIdentity;
                }

                LOGGER.debug("[EXIT] [ARC-006] Principal is not an instance of JsonWebToken. Skipping role transformation.");
                return identity;
            } catch (Exception e) {
                // [0.3] Comprehensive exception auditing with explicit tag and subsystem context
                LOGGER.error("[CRITICAL FAIL] [NFR-003] Security identity augmentation failed due to token parsing error. Raw error: {}",
                        e.getMessage(), e);
                // Return original identity to allow standard authentication/authorization interceptors to handle failure safely
                return identity;
            }
        });
    }

    /**
     * Inspects diverse JWT claim topologies to extract authorization groups and roles.
     *
     * @param jwt parsed JSON Web Token
     * @return Set of extracted string role names
     */
    private Set<String> extractRolesFromJwt(JsonWebToken jwt) {
        Set<String> roles = new HashSet<>();

        // 1. Inspect 'groups' claim (Standard MicroProfile JWT collection format)
        // [ARC-006] Extracting group claims
        Set<String> groupsClaim = jwt.getGroups();
        if (groupsClaim != null && !groupsClaim.isEmpty()) {
            roles.addAll(groupsClaim);
        }

        // 2. Inspect singular 'group' claim
        Object singularGroup = jwt.getClaim(CLAIM_GROUP);
        if (singularGroup instanceof String groupStr && !groupStr.trim().isEmpty()) {
            roles.add(groupStr.trim());
        } else if (singularGroup instanceof Collection<?> groupColl) {
            groupColl.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        // 3. Inspect custom 'roles' claim collection
        Object rolesClaim = jwt.getClaim(CLAIM_ROLES);
        if (rolesClaim instanceof Collection<?> rolesColl) {
            rolesColl.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        // 4. Inspect singular 'role' claim
        Object singularRole = jwt.getClaim(CLAIM_ROLE);
        if (singularRole instanceof String roleStr && !roleStr.trim().isEmpty()) {
            roles.add(roleStr.trim());
        }

        return Collections.unmodifiableSet(roles);
    }

    /**
     * Inspects JWTCallerPrincipal claim topologies to extract roles.
     *
     * @param callerPrincipal Caller principal instance
     * @return Set of extracted string role names
     */
    private Set<String> extractRolesFromCallerPrincipal(JWTCallerPrincipal callerPrincipal) {
        Set<String> roles = new HashSet<>();

        // Inspect 'groups' claim
        Object groupsClaim = callerPrincipal.getClaim(CLAIM_GROUPS);
        if (groupsClaim instanceof Collection<?> coll) {
            coll.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        } else if (groupsClaim instanceof String str && !str.trim().isEmpty()) {
            roles.add(str.trim());
        }

        // Inspect singular 'group' claim
        Object groupClaim = callerPrincipal.getClaim(CLAIM_GROUP);
        if (groupClaim instanceof String str && !str.trim().isEmpty()) {
            roles.add(str.trim());
        }

        // Inspect 'roles' claim
        Object rolesClaim = callerPrincipal.getClaim(CLAIM_ROLES);
        if (rolesClaim instanceof Collection<?> coll) {
            coll.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        return Collections.unmodifiableSet(roles);
    }
}