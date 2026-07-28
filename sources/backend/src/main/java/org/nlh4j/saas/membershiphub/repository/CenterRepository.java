package org.nlh4j.saas.membershiphub.repository;

import java.util.List;

import org.nlh4j.saas.membershiphub.domain.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Center} entities.
 * <p>
 * Provides CRUD operations and tenant-aware queries.
 * </p>
 */
@Repository
public interface CenterRepository extends JpaRepository<Center, Long> {

    /**
     * Retrieves all centers belonging to the specified tenant.
     *
     * @param tenantId the tenant identifier
     * @return a list of centers for the tenant
     */
    @Query("SELECT c FROM Center c WHERE c.tenantId = :tenantId AND c.isDeleted = false")
    List<Center> findByTenantId(@Param("tenantId") String tenantId);
}