package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ForbiddenException;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.model.RouteSheetStatus;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.domain.repository.RouteSheetRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import com.goslogic.orion.dispatch.presentation.dto.CreateRouteSheetRequest;
import com.goslogic.orion.dispatch.presentation.dto.CreateStopRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class RouteSheetApplicationService {

    private final RouteSheetRepository routeSheetRepository;
    private final TripStopRepository tripStopRepository;
    private final DomainEventPublisher eventPublisher;

    public RouteSheetApplicationService(RouteSheetRepository routeSheetRepository,
                                        TripStopRepository tripStopRepository,
                                        DomainEventPublisher eventPublisher) {
        this.routeSheetRepository = routeSheetRepository;
        this.tripStopRepository = tripStopRepository;
        this.eventPublisher = eventPublisher;
    }

    public RouteSheet create(CreateRouteSheetRequest req, String tenantExternalId, String roles) {
        requireFleetManagerOrAdmin(roles);

        LocalDate scheduledDate = LocalDate.parse(req.scheduledDate());
        String externalId = "route-" + UUID.randomUUID().toString().substring(0, 8);

        RouteSheet sheet = new RouteSheet(
                externalId,
                tenantExternalId,
                req.driverExternalId(),
                req.vehicleExternalId(),
                req.vehiclePlate(),
                req.vehicleModel(),
                scheduledDate
        );
        sheet.setStatus(RouteSheetStatus.ASSIGNED);
        routeSheetRepository.save(sheet);

        if (req.stops() != null) {
            for (CreateStopRequest stopReq : req.stops()) {
                String stopId = "stop-" + UUID.randomUUID().toString().substring(0, 8);
                TripStop stop = new TripStop(
                        stopId,
                        sheet,
                        stopReq.locationName(),
                        stopReq.address(),
                        stopReq.latitude(),
                        stopReq.longitude(),
                        stopReq.stopOrder(),
                        LocalDateTime.now().plusHours(stopReq.stopOrder())
                );
                tripStopRepository.save(stop);
            }
        }

        eventPublisher.publish("orion.dispatch.route-assigned", Map.of(
                "route_sheet_id", sheet.getExternalId(),
                "driver_id", sheet.getDriverExternalId(),
                "vehicle_id", sheet.getVehicleExternalId(),
                "tenant_id", sheet.getTenantExternalId()
        ));

        return sheet;
    }

    /**
     * Inicia la jornada (PATCH .../start).
     * Idempotente: si ya está IN_PROGRESS devuelve la hoja sin error.
     * Valida que el conductor del header coincida con el asignado a la hoja.
     */
    public RouteSheet startJornada(String externalId, String tenantExternalId, String driverExternalId) {
        RouteSheet sheet = findSheet(externalId, tenantExternalId);
        validateDriver(sheet, driverExternalId);
        sheet.start();
        routeSheetRepository.save(sheet);
        eventPublisher.publish("orion.dispatch.jornada-started", Map.of(
                "route_sheet_id", sheet.getExternalId(),
                "driver_id", sheet.getDriverExternalId(),
                "tenant_id", sheet.getTenantExternalId()
        ));
        return sheet;
    }

    /**
     * Finaliza la jornada (PATCH .../end).
     * Idempotente: si ya está COMPLETED devuelve la hoja sin error.
     */
    public RouteSheet endJornada(String externalId, String tenantExternalId, String driverExternalId) {
        RouteSheet sheet = findSheet(externalId, tenantExternalId);
        validateDriver(sheet, driverExternalId);
        sheet.complete();
        routeSheetRepository.save(sheet);
        eventPublisher.publish("orion.dispatch.jornada-ended", Map.of(
                "route_sheet_id", sheet.getExternalId(),
                "driver_id", sheet.getDriverExternalId(),
                "tenant_id", sheet.getTenantExternalId()
        ));
        return sheet;
    }

    @Transactional(readOnly = true)
    public List<RouteSheet> listForDriver(String tenantExternalId, String driverExternalId) {
        return routeSheetRepository.findByDriverExternalIdAndTenantExternalId(driverExternalId, tenantExternalId);
    }

    @Transactional(readOnly = true)
    public List<RouteSheet> listForTenant(String tenantExternalId) {
        return routeSheetRepository.findByTenantExternalId(tenantExternalId);
    }

    private RouteSheet findSheet(String externalId, String tenantExternalId) {
        return routeSheetRepository
                .findByExternalIdAndTenantExternalId(externalId, tenantExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RouteSheet no encontrada: " + externalId));
    }

    private void validateDriver(RouteSheet sheet, String driverExternalId) {
        if (driverExternalId != null && !driverExternalId.equals(sheet.getDriverExternalId())) {
            throw new ForbiddenException(
                    "El conductor del token no coincide con el asignado a la hoja de ruta");
        }
    }

    private void requireFleetManagerOrAdmin(String roles) {
        if (roles == null || (!roles.contains("FLEET_MANAGER") && !roles.contains("ADMIN"))) {
            throw new ForbiddenException("Solo gestores de flota o administradores pueden crear hojas de ruta");
        }
    }
}
