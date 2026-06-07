package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.RouteSheetApplicationService;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.presentation.dto.RouteSheetResponse;
import com.goslogic.orion.dispatch.presentation.dto.StatusResponse;
import com.goslogic.orion.dispatch.presentation.dto.StatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/dispatch/route-sheets")
@Tag(name = "Route Sheets", description = "Gestión de hojas de ruta del conductor")
public class RouteSheetController {

    private final RouteSheetApplicationService routeSheetService;

    public RouteSheetController(RouteSheetApplicationService routeSheetService) {
        this.routeSheetService = routeSheetService;
    }

    @GetMapping
    @Operation(summary = "Listar hojas de ruta del conductor autenticado")
    public ResponseEntity<List<RouteSheetResponse>> listForDriver(
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId) {
        List<RouteSheetResponse> result = routeSheetService
                .listForDriver(tenantExternalId, driverExternalId)
                .stream()
                .map(RouteSheetResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{routeSheetId}/start")
    @Operation(summary = "Iniciar jornada — PATCH con {\"status\": \"in_progress\"}")
    public ResponseEntity<StatusResponse> startJornada(
            @PathVariable String routeSheetId,
            @Valid @RequestBody StatusUpdateRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId) {
        RouteSheet sheet = routeSheetService.startJornada(routeSheetId, tenantExternalId, driverExternalId);
        return ResponseEntity.ok(new StatusResponse(sheet.getExternalId(), sheet.getStatus().name()));
    }

    @PatchMapping("/{routeSheetId}/end")
    @Operation(summary = "Finalizar jornada — PATCH con {\"status\": \"completed\"}")
    public ResponseEntity<StatusResponse> endJornada(
            @PathVariable String routeSheetId,
            @Valid @RequestBody StatusUpdateRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId) {
        RouteSheet sheet = routeSheetService.endJornada(routeSheetId, tenantExternalId, driverExternalId);
        return ResponseEntity.ok(new StatusResponse(sheet.getExternalId(), sheet.getStatus().name()));
    }
}
