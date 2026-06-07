package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ForbiddenException;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.repository.RouteSheetRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RouteSheetApplicationService {

    private final RouteSheetRepository routeSheetRepository;
    private final DomainEventPublisher eventPublisher;

    public RouteSheetApplicationService(RouteSheetRepository routeSheetRepository,
                                        DomainEventPublisher eventPublisher) {
        this.routeSheetRepository = routeSheetRepository;
        this.eventPublisher = eventPublisher;
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
}
