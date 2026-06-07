package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.dispatch.domain.model.TripStop;

/**
 * Respuesta del GET /dispatch/trip-stops.
 * El móvil lee tanto "sequence" como "stop_order"; se devuelven ambos con el mismo valor.
 * El status se devuelve en UPPERCASE (el móvil lo espera así para TripStop).
 */
public record TripStopResponse(
        @JsonProperty("id")              String externalId,
        @JsonProperty("route_sheet_id")  String routeSheetExternalId,
        Integer sequence,
        @JsonProperty("stop_order")      Integer stopOrder,
        String address,
        @JsonProperty("location_name")   String locationName,
        @JsonProperty("estimated_arrival") String estimatedArrival,
        String status
) {
    public static TripStopResponse from(TripStop stop) {
        return new TripStopResponse(
                stop.getExternalId(),
                stop.getRouteSheet().getExternalId(),
                stop.getStopOrder(),
                stop.getStopOrder(),
                stop.getAddress(),
                stop.getLocationName(),
                stop.getEstimatedArrival() != null ? stop.getEstimatedArrival().toString() : null,
                stop.getStatus().name()
        );
    }
}
