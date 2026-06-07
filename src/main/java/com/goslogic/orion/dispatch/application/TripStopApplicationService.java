package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TripStopApplicationService {

    private final TripStopRepository tripStopRepository;

    public TripStopApplicationService(TripStopRepository tripStopRepository) {
        this.tripStopRepository = tripStopRepository;
    }

    /**
     * Registra la llegada del conductor a la parada (PATCH .../arrived).
     * Idempotente: si ya está ARRIVED o COMPLETED no hace nada.
     */
    public TripStop markArrived(String externalId, String tenantExternalId) {
        TripStop stop = tripStopRepository
                .findByExternalIdAndRouteSheet_TenantExternalId(externalId, tenantExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TripStop no encontrada: " + externalId));
        stop.arrive();
        return tripStopRepository.save(stop);
    }

    @Transactional(readOnly = true)
    public List<TripStop> listByRouteSheet(String routeSheetExternalId) {
        return tripStopRepository.findByRouteSheet_ExternalIdOrderByStopOrderAsc(routeSheetExternalId);
    }
}
