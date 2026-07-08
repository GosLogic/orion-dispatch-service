package com.goslogic.orion.dispatch.domain.repository;

import com.goslogic.orion.dispatch.domain.model.TripStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {

    Optional<TripStop> findByExternalIdAndRouteSheet_TenantExternalId(String externalId, String tenantExternalId);

    boolean existsByExternalId(String externalId);

    @EntityGraph(attributePaths = "routeSheet")
    List<TripStop> findByRouteSheet_ExternalIdOrderByStopOrderAsc(String routeSheetExternalId);
}
