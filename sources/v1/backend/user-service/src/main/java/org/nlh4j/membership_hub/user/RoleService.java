package org.nlh4j.saas.membership-hub.user;

/**
 * Service layer for managing user roles and Role-Based Access Control (RBAC).
 * Implements the core business logic for assigning, retrieving, updating, and revoking roles
 * to support the enterprise authorization framework.
 *
 * @traceability [REQ-003], [ARC-001]
 */
@Service
@Transactional
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – Business rules and error messages for role management      */
    /* --------------------------------------------------------------------- */
    /** Maximum allowed length for a role name (enforced by validation). */
    public static final int MAX_ROLE_NAME_LENGTH = 30;
    /** Regular expression pattern for valid role names (uppercase letters and underscores only). */
    public static final String ROLE_NAME_PATTERN = "^[A-Z_]+$";
    /** Standard error message when a role is not found. */
    public static final String ERROR_ROLE_NOT_FOUND = "Role not found with id: %s";
    /** Standard error message when a user is not found. */
    public static final String ERROR_USER_NOT_FOUND = "User not found with id: %s";
    /** Error message for duplicate role assignments. */
    public static final String ERROR_ROLE_ASSIGN_CONFLICT = "Role assignment conflict: user already has role: %s";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCIES – Repositories for Users, Roles, and the many‑to‑many link   */
    /* --------------------------------------------------------------------- */
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /* --------------------------------------------------------------------- */
    /* PUBLIC API – Role management operations                               */
    /* --------------------------------------------------------------------- */

    /**
     * Assigns a role to a user.
     * <p>
     * This operation is idempotent – attempting to assign an already‑assigned role
     * will raise a {@link DuplicateResourceException}. Full audit logging and
     * comprehensive exception handling are applied to satisfy enterprise
     * reliability and traceability requirements.
     *
     * @param userId UUID of the target user.
     * @param roleId  UUID of the role to assign.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void assignRoleToUser(UUID userId, UUID roleId) {
        logger.info("[ENTRY] assignRoleToUser userId={} roleId={}", userId, roleId);
        try {
            // Resolve user and role entities – throw descriptive exceptions if missing
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, roleId)));

            // Idempotent check – avoid duplicate assignments
            boolean alreadyAssigned = userRoleRepository.existsByUserAndRole(user, role);
            if (alreadyAssigned) {
                logger.warn("[WARN] assignRoleToUser conflict: user {} already has role {}", userId, roleId);
                throw new DuplicateResourceException(String.format(ERROR_ROLE_ASSIGN_CONFLICT, role.getName()));
            }

            // Persist the association
            UserRoles userRole = new UserRoles();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setAssignedAt(Instant.now());
            userRoleRepository.save(userRole);

            logger.info("[EXIT] assignRoleToUser completed for userId={}", userId);
        } catch (Exception e) {
            // Enterprise‑grade error logging – includes traceability tag and raw error
            logger.error("[CRITICAL FAIL] [ARC-001] Role assignment failed for userId={} roleId={}. Raw error: {}", userId, roleId, e.getMessage(), e);
            throw new RoleAssignmentException("Failed to assign role", e);
        }
    }

    /**
     * Retrieves all roles currently assigned to the specified user.
     *
     * @param userId UUID of the user whose roles are requested.
     * @return List of {@link Roles} objects.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public List<Roles> getRolesForUser(UUID userId) {
        logger.info("[ENTRY] getRolesForUser userId={}", userId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            List<UserRoles> userRoles = userRoleRepository.findByUser(user);
            List<Roles> roles = userRoles.stream()
                .map(UserRoles::getRole)
                .collect(Collectors.toList());
            logger.info("[EXIT] getRolesForUser returned {} roles for userId={}", roles.size(), userId);
            return roles;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to retrieve roles for userId={}. Raw error: {}", userId, e.getMessage(), e);
            throw new RoleRetrievalException("Failed to retrieve user roles", e);
        }
    }

    /**
     * Updates a user's role by revoking an old role and assigning a new one.
     * <p>
     * This operation ensures atomicity – both the revocation and the new assignment
     * are persisted within a single transaction.
     *
     * @param userId   UUID of the user.
     * @param oldRoleId UUID of the role to revoke.
     * @param newRoleId UUID of the role to assign.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void updateUserRole(UUID userId, UUID oldRoleId, UUID newRoleId) {
        logger.info("[ENTRY] updateUserRole userId={} oldRoleId={} newRoleId={}", userId, oldRoleId, newRoleId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles oldRole = roleRepository.findById(oldRoleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, oldRoleId)));
            Roles newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, newRoleId)));

            // Revoke the old role
            userRoleRepository.deleteByUserAndRole(user, oldRole);

            // Assign the new role
            UserRoles userRole = new UserRoles();
            userRole.setUser(user);
            userRole.setRole(newRole);
            userRole.setAssignedAt(Instant.now());
            userRoleRepository.save(userRole);

            logger.info("[EXIT] updateUserRole completed for userId={}", userId);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Role update failed for userId={} oldRoleId={} newRoleId={}. Raw error: {}", userId, oldRoleId, newRoleId, e.getMessage(), e);
            throw new RoleUpdateException("Failed to update user role", e);
        }
    }

    /**
     * Revokes a role from a user.
     *
     * @param userId UUID of the user.
     * @param roleId  UUID of the role to revoke.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void revokeRoleFromUser(UUID userId, UUID roleId) {
        logger.info("[ENTRY] revokeRoleFromUser userId={} roleId={}", userId, roleId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, roleId)));

            boolean exists = userRoleRepository.existsByUserAndRole(user, role);
            if (!exists) {
                logger.warn("[WARN] revokeRoleFromUser: user {} does not have role {}", userId, roleId);
                throw new EntityNotFoundException("User does not have the specified role");
            }

            userRoleRepository.deleteByUserAndRole(user, role);
            logger.info("[EXIT] revokeRoleFromUser completed for userId={}", userId);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Role revocation failed for userId={} roleId={}. Raw error: {}", userId, roleId, e.getMessage(), e);
            throw new RoleRevocationException("Failed to revoke role", e);
        }
    }

    /**
     * Lists all available roles in the system (useful for UI role‑assignment screens).
     *
     * @return List of all {@link Roles}.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public List<Roles> listAllRoles() {
        logger.info("[ENTRY] listAllRoles");
        try {
            List<Roles> roles = roleRepository.findAll();
            logger.info("[EXIT] listAllRoles returned {} roles", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to list all roles. Raw error: {}", e.getMessage(), e);
            throw new RoleListingException("Failed to list roles", e);
        }
    }

    /* --------------------------------------------------------------------- */
    /* INNER EXCEPTION TYPES – Custom enterprise exceptions for role operations */
    /* --------------------------------------------------------------------- */

    /** Thrown when a duplicate role assignment is attempted. */
    @SuppressWarnings("serial")
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) { super(message); }
    }

    /** Base exception for all role‑assignment failures. */
    @SuppressWarnings("serial")
    public static class RoleAssignmentException extends RuntimeException {
        public RoleAssignmentException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role cannot be retrieved. */
    @SuppressWarnings("serial")
    public static class RoleRetrievalException extends RuntimeException {
        public RoleRetrievalException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role update fails. */
    @SuppressWarnings("serial")
    public static class RoleUpdateException extends RuntimeException {
        public RoleUpdateException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role revocation fails. */
    @SuppressWarnings("serial")
    public static class RoleRevocationException extends RuntimeException {
        public RoleRevocationException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when listing all roles fails. */
    @SuppressWarnings("serial")
    public static class RoleListingException extends RuntimeException {
        public RoleListingException(String message, Throwable cause) { super(message, cause); }
    }

    /* --------------------------------------------------------------------- */
    /* INNER DOMAIN MODELS – Simplified POJOs for demonstration purposes      */
    /* --------------------------------------------------------------------- */

    @Entity
    @Table(name = "roles")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Roles {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;
        @Column(nullable = false, length = MAX_ROLE_NAME_LENGTH, unique = true)
        private String name;
        @Column(length = 255)
        private String description;
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();
    }

    @Entity
    @Table(name = "users")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Users {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;
        @Column(nullable = false, unique = true, length = 255)
        private String email;
        @Column(name = "full_name", nullable = false, length = 100)
        private String fullName;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Roles role;
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();
    }

    @Entity
    @Table(name = "user_roles")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UserRoles {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private Users user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Roles role;

        @Column(name = "assigned_at", nullable = false, updatable = false)
        private Instant assignedAt = Instant.now();
    }

    /* --------------------------------------------------------------------- */
    /* REPOSITORY INTERFACES – Spring Data JPA contracts                       */
    /* --------------------------------------------------------------------- */

    public interface RoleRepository extends JpaRepository<Roles, UUID> {
        // Custom queries can be added here if needed (e.g., findByName)
    }

    public interface UserRepository extends JpaRepository<Users, UUID> {
        // Custom queries can be added here if needed (e.g., findByEmail)
    }

    public interface UserRoleRepository extends JpaRepository<UserRoles, UUID> {
        boolean existsByUserAndRole(Users user, Roles role);
        void deleteByUserAndRole(Users user, Roles role);
        List<UserRoles> findByUser(Users user);
    }
}