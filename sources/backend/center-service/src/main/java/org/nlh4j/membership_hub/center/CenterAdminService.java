package org.nlh4j.saas.membership_hub.center;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.nlh4j.saas.membership_hub.user.UserRepository;
import org.nlh4j.saas.membership_hub.user.User;
import org.nlh4j.saas.membership_hub.center.CenterRepository;
import org.nlh4j.saas.membership_hub.center.Center;
import org.nlh4j.saas.membership_hub.center.CenterAdminRepository;
import org.nlh4j.saas.membership_hub.center.CenterAdmin;

/**
 * Service responsible for assigning and unassigning center administrators.
 *
 * <p>
 * This service implements the business logic for the {@code /admin/centers/{centerId}/admins}
 * endpoint. It ensures that only existing users can be granted or revoked the
 * {@code Center Admin} role for a specific center. All operations are performed
 * within a single transaction to guarantee consistency.
 * </p>
 *
 * @traceability [REQ-006], [ARC-002]
 */
@Service
public class CenterAdminService {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */

    /** Message key for user not found error. */
    private static final String MSG_USER_NOT_FOUND = "User with ID %d does not exist";

    /** Message key for center not found error. */
    private static final String MSG_CENTER_NOT_FOUND = "Center with ID %d does not exist";

    /** Message key for duplicate assignment error. */
    private static final String MSG_ALREADY_ASSIGNED = "User %d is already a Center Admin for center %d";

    /** Message key for successful assignment. */
    private static final String MSG_ASSIGN_SUCCESS = "User %d assigned as Center Admin for center %d";

    /** Message key for successful unassignment. */
    private static final String MSG_UNASSIGN_SUCCESS = "User %d unassigned from Center Admin role for center %d";

    /* --------------------------------------------------------------------- */
    /*  Logger (audit trail)                                                */
    /* --------------------------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(CenterAdminService.class);

    /* --------------------------------------------------------------------- */
    /*  Repositories (injected by Spring)                                  */
    /* --------------------------------------------------------------------- */

    private final UserRepository userRepository;
    private final CenterRepository centerRepository;
    private final CenterAdminRepository centerAdminRepository;

    /**
     * Constructor injection of required repositories.
     *
     * @param userRepository          repository for {@link User} entities
     * @param centerRepository        repository for {@link Center} entities
     * @param centerAdminRepository   repository for {@link CenterAdmin} entities
     */
    public CenterAdminService(UserRepository userRepository,
                              CenterRepository centerRepository,
                              CenterAdminRepository centerAdminRepository) {
        this.userRepository = userRepository;
        this.centerRepository = centerRepository;
        this.centerAdminRepository = centerAdminRepository;
    }

    /* --------------------------------------------------------------------- */
    /*  Public API                                                          */
    /* --------------------------------------------------------------------- */

    /**
     * Assigns a user as a Center Administrator for the specified center.
     *
     * <p>
     * The method performs the following steps atomically:
     * <ul>
     *   <li>Validate that the center exists.</li>
     *   <li>Validate that the user exists.</li>
     *   <li>Check for an existing assignment to avoid duplicates.</li>
     *   <li>Persist a new {@link CenterAdmin} record.</li>
     * </ul>
     * </p>
     *
     * @param centerId the ID of the center
     * @param userId   the ID of the user to assign
     * @throws IllegalArgumentException if the center or user does not exist,
     *                                  or if the assignment already exists
     */
    @Transactional
    public void assignCenterAdmin(Long centerId, Long userId) {
        logger.info("[PROCESS] Assigning user {} as Center Admin for center {}", userId, centerId);

        // Validate center existence
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_CENTER_NOT_FOUND, centerId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_USER_NOT_FOUND, userId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Prevent duplicate assignment
        Optional<CenterAdmin> existing = centerAdminRepository.findByCenterAndUser(center, user);
        if (existing.isPresent()) {
            String msg = String.format(MSG_ALREADY_ASSIGNED, userId, centerId);
            logger.warn("[WARN] {}", msg);
            throw new IllegalStateException(msg);
        }

        // Persist new assignment
        CenterAdmin assignment = new CenterAdmin();
        assignment.setCenter(center);
        assignment.setUser(user);
        centerAdminRepository.save(assignment);

        logger.info("[SUCCESS] {}", String.format(MSG_ASSIGN_SUCCESS, userId, centerId));
    }

    /**
     * Unassigns a user from the Center Administrator role for the specified center.
     *
     * <p>
     * The method performs the following steps atomically:
     * <ul>
     *   <li>Validate that the center exists.</li>
     *   <li>Validate that the user exists.</li>
     *   <li>Locate the existing assignment.</li>
     *   <li>Delete the assignment record.</li>
     * </ul>
     * </p>
     *
     * @param centerId the ID of the center
     * @param userId   the ID of the user to unassign
     * @throws IllegalArgumentException if the center or user does not exist,
     *                                  or if no assignment exists
     */
    @Transactional
    public void unassignCenterAdmin(Long centerId, Long userId) {
        logger.info("[PROCESS] Unassigning user {} from Center Admin role for center {}", userId, centerId);

        // Validate center existence
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_CENTER_NOT_FOUND, centerId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_USER_NOT_FOUND, userId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Locate assignment
        CenterAdmin assignment = centerAdminRepository.findByCenterAndUser(center, user)
                .orElseThrow(() -> {
                    String msg = String.format("No Center Admin assignment found for user %d and center %d", userId, centerId);
                    logger.warn("[WARN] {}", msg);
                    return new IllegalStateException(msg);
                });

        // Delete assignment
        centerAdminRepository.delete(assignment);

        logger.info("[SUCCESS] {}", String.format(MSG_UNASSIGN_SUCCESS, userId, centerId));
    }
}