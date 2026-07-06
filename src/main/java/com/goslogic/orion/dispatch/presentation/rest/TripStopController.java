package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.TripStopApplicationService;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.presentation.dto.StatusResponse;
import com.goslogic.orion.dispatch.presentation.dto.StatusUpdateRequest;
import com.goslogic.orion.dispatch.presentation.dto.TripStopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/dispatch/trip-stops")
@Tag(name = "Trip Stops", description = "Paradas de la hoja de ruta")
public class TripStopController {

    private final TripStopApplicationService tripStopService;

    public TripStopController(TripStopApplicationService tripStopService) {
        this.tripStopService = tripStopService;
    }

    @GetMapping
    @Operation(summary = "Listar paradas de una hoja de ruta (con entregas anidadas)")
    public ResponseEntity<List<TripStopResponse>> listByRouteSheet(
            @RequestParam("route_sheet_id") String routeSheetId) {
        return ResponseEntity.ok(tripStopService.listByRouteSheetWithDeliveries(routeSheetId));
    }

    @PatchMapping("/{tripStopId}/arrived")
    @Operation(summary = "Registrar llegada del conductor a la parada — PATCH con {\"status\": \"arrived\"}")
    public ResponseEntity<StatusResponse> markArrived(
            @PathVariable String tripStopId,
            @Valid @RequestBody StatusUpdateRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        TripStop stop = tripStopService.markArrived(
                tripStopId, tenantExternalId, req.latitude(), req.longitude());
        return ResponseEntity.ok(new StatusResponse(stop.getExternalId(), stop.getStatus().name()));
    }
}
