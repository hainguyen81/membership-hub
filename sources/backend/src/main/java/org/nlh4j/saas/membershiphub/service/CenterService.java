package org.nlh4j.saas.membershiphub.service;

import java.util.List;
import java.util.Optional;

import org.nlh4j.saas.membershiphub.domain.Center;
import org.nlh4j.saas.membershiphub.domain.Role;
import org.nlh4j.saas.membershiphub.repository.CenterRepository;
import org.nlh4j.saas.membershiphub.exception.CenterNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for {@link Center} entity.
 * <p>
 * Business rules:
 * <ul>
 *   <li>Only users with {@link Role#SYSTEM_ADMIN} can manage centers across tenants.</li>
 *   <li>All CRUD operations are audited via {@link Logger}.</li>
 *   <li>All queries use parameter binding to prevent SQL injection.</li>
 * </ul>
 */
@Service
public class CenterService {

    private static final Logger LOG = LoggerFactory.getLogger(CenterService.class);

    private final CenterRepository centerRepository;
    private final CurrentUserService currentUserService;

    public CenterService(CenterRepository centerRepository, CurrentUserService currentUserService) {
        this.centerRepository = centerRepository;
        this.currentUserService = currentUserService;
    }

    /* --------------------------------------------------------------------- */
    /* CRUD Operations                                                        */
    /* --------------------------------------------------------------------- */

    @Transactional
    public Center createCenter(Center center) {
        enforceTenantScope(center.getTenantId());
        Center saved = centerRepository.save(center);
        LOG.info("Center created: id={}, tenantId={}", saved.getId(), saved.getTenantId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Center getCenter(Long id) {
        Center center = centerRepository.findById(id)
                .orElseThrow(() -> new CenterNotFoundException("Center not found: id=" + id));
        enforceTenantScope(center.getTenantId());
        return center;
    }

    @Transactional
    public Center updateCenter(Long id, Center updated) {
        Center existing = getCenter(id);
        enforceTenantScope(updated.getTenantId());
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setTaxId(updated.getTaxId());
        existing.setContactPhone(updated.getContactPhone());
        existing.setUpdatedAt(updated.getUpdatedAt());
        existing.setTenantId(updated.getTenantId());
        Center saved = centerRepository.save(existing);
        LOG.info("Center updated: id={}, tenantId={}", saved.getId(), saved.getTenantId());
        return saved;
    }

    @Transactional
    public void deleteCenter(Long id) {
        Center center = getCenter(id);
        centerRepository.deleteById(id);
        LOG.info("Center deleted: id={}, tenantId={}", id, center.getTenantId());
    }

    /* --------------------------------------------------------------------- */
    /* Query Operations                                                       */
    /* --------------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public List<Center> findAllCenters() {
        String tenantId = currentUserService.getTenantId();
        if (currentUserService.isSystemAdmin()) {
            return centerRepository.findAll();
        }
        return centerRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Center> findCentersByTenant(String tenantId) {
        if (!currentUserService.isSystemAdmin()) {
            throw new IllegalStateException("Only System Admin can query centers across tenants");
        }
        return centerRepository.findByTenantId(tenantId);
    }

    /* --------------------------------------------------------------------- */
    /* Helper Methods                                                         */
    /* --------------------------------------------------------------------- */

    /**
     * Enforces that the current user can only operate on the specified tenant.
     * System Admins are exempt.
     *
     * @param tenantId the tenant id to check
     */
    private void enforceTenantScope(String tenantId) {
        if (!currentUserService.isSystemAdmin()
                && !currentUserService.getTenantId().equals(tenantId)) {
            throw new IllegalStateException(
                    "User does not have permission to modify data for tenant: " + tenantId);
        }
    }
}