package com.goslogic.orion.dispatch.domain.repository;

import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteSheetRepository extends JpaRepository<RouteSheet, Long> {

    Optional<RouteSheet> findByExternalIdAndTenantExternalId(String externalId, String tenantExternalId);

    boolean existsByExternalId(String externalId);

    List<RouteSheet> findByDriverExternalIdAndTenantExternalId(String driverExternalId, String tenantExternalId);

    List<RouteSheet> findByTenantExternalId(String tenantExternalId);
}
