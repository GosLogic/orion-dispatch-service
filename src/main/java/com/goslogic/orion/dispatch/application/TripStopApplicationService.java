package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.Delivery;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.domain.repository.DeliveryRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.presentation.dto.DeliverySummaryResponse;
import com.goslogic.orion.dispatch.presentation.dto.TripStopResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripStopApplicationService {

    private final TripStopRepository tripStopRepository;
    private final DeliveryRepository deliveryRepository;

    public TripStopApplicationService(TripStopRepository tripStopRepository,
                                      DeliveryRepository deliveryRepository) {
        this.tripStopRepository = tripStopRepository;
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Registra la llegada del conductor a la parada (PATCH .../arrived).
     * Idempotente: si ya está ARRIVED o COMPLETED no hace nada.
     */
    public TripStop markArrived(String externalId, String tenantExternalId,
                                Double latitude, Double longitude) {
        TripStop stop = tripStopRepository
                .findByExternalIdAndRouteSheet_TenantExternalId(externalId, tenantExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TripStop no encontrada: " + externalId));
        stop.arrive(latitude, longitude);
        return tripStopRepository.save(stop);
    }

    @Transactional(readOnly = true)
    public List<TripStop> listByRouteSheet(String routeSheetExternalId) {
        return tripStopRepository.findByRouteSheet_ExternalIdOrderByStopOrderAsc(routeSheetExternalId);
    }

    @Transactional(readOnly = true)
    public List<TripStopResponse> listByRouteSheetWithDeliveries(String routeSheetExternalId) {
        List<TripStop> stops = listByRouteSheet(routeSheetExternalId);
        List<Delivery> deliveries = deliveryRepository
                .findByTripStop_RouteSheet_ExternalIdOrderByTripStop_StopOrderAsc(routeSheetExternalId);

        Map<String, List<DeliverySummaryResponse>> byStop = deliveries.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTripStop().getExternalId(),
                        Collectors.mapping(DeliverySummaryResponse::from, Collectors.toList())
                ));

        return stops.stream()
                .map(stop -> TripStopResponse.from(
                        stop,
                        byStop.getOrDefault(stop.getExternalId(), List.of())
                ))
                .toList();
    }
}
