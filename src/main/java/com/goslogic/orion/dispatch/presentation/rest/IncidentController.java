package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.IncidentApplicationService;
import com.goslogic.orion.dispatch.application.IncidentApplicationService.CreateIncidentCommand;
import com.goslogic.orion.dispatch.domain.model.Incident;
import com.goslogic.orion.dispatch.presentation.dto.CreateIncidentRequest;
import com.goslogic.orion.dispatch.presentation.dto.IncidentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/incidents")
@Tag(name = "Incidents", description = "Incidentes de ruta y alertas de pánico")
public class IncidentController {

    private final IncidentApplicationService incidentService;

    public IncidentController(IncidentApplicationService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @Operation(summary = "Reportar incidente de ruta")
    public ResponseEntity<IncidentResponse> reportIncident(
            @Valid @RequestBody CreateIncidentRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId) {
        Incident incident = incidentService.reportIncident(toCommand(req, tenantExternalId, driverExternalId));
        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    @PostMapping("/panic")
    @Operation(summary = "Reportar alerta de pánico (SLA respuesta rápida)")
    public ResponseEntity<IncidentResponse> reportPanic(
            @Valid @RequestBody CreateIncidentRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverExternalId) {
        Incident incident = incidentService.reportPanic(toCommand(req, tenantExternalId, driverExternalId));
        return ResponseEntity.ok(IncidentResponse.from(incident));
    }

    private CreateIncidentCommand toCommand(CreateIncidentRequest req,
                                            String tenantExternalId,
                                            String driverExternalId) {
        return new CreateIncidentCommand(
                req.externalId(),
                req.stopExternalId(),
                req.type(),
                req.description(),
                req.reportedAt(),
                Boolean.TRUE.equals(req.panic()),
                req.latitude(),
                req.longitude(),
                tenantExternalId,
                driverExternalId != null ? driverExternalId : "unknown-driver"
        );
    }
}
