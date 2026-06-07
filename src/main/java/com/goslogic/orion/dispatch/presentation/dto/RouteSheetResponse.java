package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;

/**
 * Respuesta del GET /dispatch/route-sheets.
 * El campo "status" se devuelve en lowercase con guiones (ej. "in_progress") para
 * compatibilidad con el parser del móvil (movil.md §4 — RouteSheetStatus.toJson()).
 */
public record RouteSheetResponse(
        @JsonProperty("id")          String externalId,
        @JsonProperty("tenant_id")   String tenantExternalId,
        @JsonProperty("driver_id")   String driverExternalId,
        @JsonProperty("vehicle_id")  String vehicleExternalId,
        @JsonProperty("vehicle_plate")  String vehiclePlate,
        @JsonProperty("vehicle_model")  String vehicleModel,
        String status,
        @JsonProperty("scheduled_date") String scheduledDate
) {
    public static RouteSheetResponse from(RouteSheet rs) {
        return new RouteSheetResponse(
                rs.getExternalId(),
                rs.getTenantExternalId(),
                rs.getDriverExternalId(),
                rs.getVehicleExternalId(),
                rs.getVehiclePlate(),
                rs.getVehicleModel(),
                rs.getStatus().name().toLowerCase(),
                rs.getDate() != null ? rs.getDate().toString() : null
        );
    }
}
