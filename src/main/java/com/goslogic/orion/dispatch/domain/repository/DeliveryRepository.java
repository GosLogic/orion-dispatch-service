package com.goslogic.orion.dispatch.domain.repository;

import com.goslogic.orion.dispatch.domain.model.Delivery;
import com.goslogic.orion.dispatch.domain.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    List<Delivery> findByTripStop_Id(Long tripStopId);

    List<Delivery> findByTripStop_RouteSheet_ExternalIdOrderByTripStop_StopOrderAsc(String routeSheetExternalId);

    long countByTripStop_IdAndStatusNot(Long tripStopId, DeliveryStatus status);
}
