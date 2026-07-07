package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.RouteSheetApplicationService;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.presentation.dto.CreateRouteSheetRequest;
import com.goslogic.orion.dispatch.presentation.dto.RouteSheetResponse;
import com.goslogic.orion.dispatch.presentation.dto.StatusResponse;
import com.goslogic.orion.dispatch.presentation.dto.StatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Listar hojas de ruta (conductor o gestor de flota)")
    public ResponseEntity<List<RouteSheetResponse>> list(
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId,
            @RequestHeader(value = "X-Roles", required = false) String roles) {
        List<RouteSheet> sheets = shouldListByTenant(roles, driverExternalId)
                ? routeSheetService.listForTenant(tenantExternalId)
                : routeSheetService.listForDriver(tenantExternalId, driverExternalId);
        List<RouteSheetResponse> result = sheets.stream()
                .map(RouteSheetResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    private boolean shouldListByTenant(String roles, String driverExternalId) {
        if (driverExternalId != null && !driverExternalId.isBlank()) {
            return false;
        }
        return roles != null && (roles.contains("FLEET_MANAGER") || roles.contains("ADMIN"));
    }

    @PostMapping
    @Operation(summary = "Crear hoja de ruta (gestor de flota o admin)")
    public ResponseEntity<RouteSheetResponse> create(
            @Valid @RequestBody CreateRouteSheetRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Roles", required = false) String roles) {
        RouteSheet sheet = routeSheetService.create(req, tenantExternalId, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteSheetResponse.from(sheet));
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
