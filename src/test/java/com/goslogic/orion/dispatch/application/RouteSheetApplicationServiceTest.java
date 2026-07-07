package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ForbiddenException;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.model.RouteSheetStatus;
import com.goslogic.orion.dispatch.domain.repository.RouteSheetRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import com.goslogic.orion.dispatch.presentation.dto.CreateRouteSheetRequest;
import com.goslogic.orion.dispatch.presentation.dto.CreateStopRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RouteSheetApplicationServiceTest {

    @Mock RouteSheetRepository routeSheetRepository;
    @Mock TripStopRepository tripStopRepository;
    @Mock DomainEventPublisher eventPublisher;

    RouteSheetApplicationService service;

    RouteSheet sheet;

    @BeforeEach
    void setUp() {
        service = new RouteSheetApplicationService(routeSheetRepository, tripStopRepository, eventPublisher);

        sheet = new RouteSheet(
                "route-demo-001", "tenant-demo", "driver-demo",
                "vehicle-001", "ABC-1234", "Mercedes Sprinter 2024",
                LocalDate.now()
        );
        sheet.setStatus(RouteSheetStatus.ASSIGNED);

        when(routeSheetRepository.findByExternalIdAndTenantExternalId("route-demo-001", "tenant-demo"))
                .thenReturn(Optional.of(sheet));
        when(routeSheetRepository.findByExternalIdAndTenantExternalId("inexistente", "tenant-demo"))
                .thenReturn(Optional.empty());
        when(routeSheetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void startJornada_cambia_estado_a_IN_PROGRESS() {
        RouteSheet result = service.startJornada("route-demo-001", "tenant-demo", "driver-demo");
        assertThat(result.getStatus()).isEqualTo(RouteSheetStatus.IN_PROGRESS);
        assertThat(result.getStartedAt()).isNotNull();
        verify(eventPublisher).publish(eq("orion.dispatch.jornada-started"), anyMap());
    }

    @Test
    void startJornada_idempotente_si_ya_es_IN_PROGRESS() {
        sheet.start();
        RouteSheet result = service.startJornada("route-demo-001", "tenant-demo", "driver-demo");
        assertThat(result.getStatus()).isEqualTo(RouteSheetStatus.IN_PROGRESS);
    }

    @Test
    void startJornada_lanza_404_si_no_existe() {
        assertThatThrownBy(() -> service.startJornada("inexistente", "tenant-demo", "driver-demo"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void startJornada_lanza_403_si_driver_no_coincide() {
        assertThatThrownBy(() -> service.startJornada("route-demo-001", "tenant-demo", "otro-driver"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void endJornada_cambia_estado_a_COMPLETED() {
        sheet.start();
        RouteSheet result = service.endJornada("route-demo-001", "tenant-demo", "driver-demo");
        assertThat(result.getStatus()).isEqualTo(RouteSheetStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(eventPublisher).publish(eq("orion.dispatch.jornada-ended"), anyMap());
    }

    @Test
    void endJornada_idempotente_si_ya_es_COMPLETED() {
        sheet.start();
        sheet.complete();
        RouteSheet result = service.endJornada("route-demo-001", "tenant-demo", "driver-demo");
        assertThat(result.getStatus()).isEqualTo(RouteSheetStatus.COMPLETED);
    }

    @Test
    void endJornada_lanza_403_si_driver_no_coincide() {
        sheet.start();
        assertThatThrownBy(() -> service.endJornada("route-demo-001", "tenant-demo", "otro-driver"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void endJornada_lanza_404_si_no_existe() {
        assertThatThrownBy(() -> service.endJornada("inexistente", "tenant-demo", "driver-demo"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void startJornada_lanza_409_si_ya_esta_COMPLETED() {
        sheet.start();
        sheet.complete();
        assertThatThrownBy(() -> service.startJornada("route-demo-001", "tenant-demo", "driver-demo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede iniciar una hoja ya completada");
    }

    @Test
    void listForDriver_retorna_hojas_del_conductor() {
        when(routeSheetRepository.findByDriverExternalIdAndTenantExternalId("driver-demo", "tenant-demo"))
                .thenReturn(List.of(sheet));

        List<RouteSheet> result = service.listForDriver("tenant-demo", "driver-demo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo("route-demo-001");
    }

    @Test
    void listForDriver_retorna_lista_vacia_si_no_hay_hojas() {
        when(routeSheetRepository.findByDriverExternalIdAndTenantExternalId("driver-demo", "tenant-demo"))
                .thenReturn(List.of());

        List<RouteSheet> result = service.listForDriver("tenant-demo", "driver-demo");

        assertThat(result).isEmpty();
    }

    @Test
    void listForTenant_retorna_hojas_del_tenant() {
        when(routeSheetRepository.findByTenantExternalId("tenant-demo"))
                .thenReturn(List.of(sheet));

        List<RouteSheet> result = service.listForTenant("tenant-demo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo("route-demo-001");
    }

    @Test
    void create_asigna_hoja_con_paradas() {
        CreateRouteSheetRequest req = new CreateRouteSheetRequest(
                "driver-demo",
                "vehicle-001",
                "ABC-1234",
                "Mercedes Sprinter",
                LocalDate.now().toString(),
                List.of(new CreateStopRequest("Cliente A", "Av. Test 1", -12.04, -77.04, 1))
        );

        RouteSheet result = service.create(req, "tenant-demo", "FLEET_MANAGER");

        assertThat(result.getStatus()).isEqualTo(RouteSheetStatus.ASSIGNED);
        assertThat(result.getDriverExternalId()).isEqualTo("driver-demo");
        verify(routeSheetRepository).save(any(RouteSheet.class));
        verify(tripStopRepository).save(any());
        verify(eventPublisher).publish(eq("orion.dispatch.route-assigned"), anyMap());
    }

    @Test
    void create_lanza_403_si_no_es_gestor() {
        CreateRouteSheetRequest req = new CreateRouteSheetRequest(
                "driver-demo", "vehicle-001", "ABC-1234", "Model", LocalDate.now().toString(), List.of());

        assertThatThrownBy(() -> service.create(req, "tenant-demo", "DRIVER"))
                .isInstanceOf(ForbiddenException.class);
    }
}
