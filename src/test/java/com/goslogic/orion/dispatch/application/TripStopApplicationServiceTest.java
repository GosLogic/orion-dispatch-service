package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.Delivery;
import com.goslogic.orion.dispatch.domain.model.DeliveryStatus;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.domain.model.TripStopStatus;
import com.goslogic.orion.dispatch.domain.repository.DeliveryRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.presentation.dto.TripStopResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
// Estas pruebas validan integridad de estado bajo concurrencia e idempotencia en sincronización móvil.
class TripStopApplicationServiceTest {

    @Mock TripStopRepository tripStopRepository;
    @Mock DeliveryRepository deliveryRepository;

    TripStopApplicationService service;

    RouteSheet sheet;
    TripStop stop;

    @BeforeEach
    void setUp() {
        service = new TripStopApplicationService(tripStopRepository, deliveryRepository);

        sheet = new RouteSheet(
                "route-demo-001", "tenant-demo", "driver-demo",
                "vehicle-001", "ABC-1234", "Mercedes Sprinter 2024",
                LocalDate.now()
        );

        stop = new TripStop(
                "stop-001", sheet,
                "Bodega Central", "Av. Industrial 1200",
                -12.0, -77.0, 1,
                LocalDateTime.now().plusHours(1)
        );

        when(tripStopRepository.findByExternalIdAndRouteSheet_TenantExternalId("stop-001", "tenant-demo"))
                .thenReturn(Optional.of(stop));
        when(tripStopRepository.findByExternalIdAndRouteSheet_TenantExternalId("stop-xxx", "tenant-demo"))
                .thenReturn(Optional.empty());
        when(tripStopRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tripStopRepository.findByRouteSheet_ExternalIdOrderByStopOrderAsc("route-demo-001"))
                .thenReturn(List.of(stop));
    }

    @Test
    void markArrived_cambia_estado_a_ARRIVED() {
        TripStop result = service.markArrived("stop-001", "tenant-demo", -12.05, -77.05);

        assertThat(result.getStatus()).isEqualTo(TripStopStatus.ARRIVED);
        assertThat(result.getArrivalTime()).isNotNull();
        assertThat(result.getArrivalLatitude()).isEqualTo(-12.05);
        assertThat(result.getArrivalLongitude()).isEqualTo(-77.05);
        verify(tripStopRepository).save(stop);
    }

    @Test
    void markArrived_idempotente_si_ya_es_ARRIVED() {
        stop.arrive();
        LocalDateTime arrivalBefore = stop.getArrivalTime();

        TripStop result = service.markArrived("stop-001", "tenant-demo", null, null);

        assertThat(result.getStatus()).isEqualTo(TripStopStatus.ARRIVED);
        assertThat(result.getArrivalTime()).isEqualTo(arrivalBefore);
    }

    @Test
    void markArrived_idempotente_si_ya_es_COMPLETED() {
        stop.arrive();
        stop.complete();

        TripStop result = service.markArrived("stop-001", "tenant-demo", null, null);

        assertThat(result.getStatus()).isEqualTo(TripStopStatus.COMPLETED);
    }

    @Test
    void markArrived_lanza_404_si_no_existe() {
        assertThatThrownBy(() -> service.markArrived("stop-xxx", "tenant-demo", null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("stop-xxx");
    }

    @Test
    void listByRouteSheet_retorna_paradas_ordenadas() {
        TripStop stop2 = new TripStop(
                "stop-002", sheet,
                "Supermercado", "Jr. Comercio 450",
                -12.1, -77.1, 2,
                LocalDateTime.now().plusHours(2)
        );
        when(tripStopRepository.findByRouteSheet_ExternalIdOrderByStopOrderAsc("route-demo-001"))
                .thenReturn(List.of(stop, stop2));

        List<TripStop> result = service.listByRouteSheet("route-demo-001");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getExternalId()).isEqualTo("stop-001");
        assertThat(result.get(1).getExternalId()).isEqualTo("stop-002");
    }

    @Test
    void listByRouteSheet_retorna_lista_vacia_si_no_hay_paradas() {
        when(tripStopRepository.findByRouteSheet_ExternalIdOrderByStopOrderAsc("route-vacia"))
                .thenReturn(List.of());

        List<TripStop> result = service.listByRouteSheet("route-vacia");

        assertThat(result).isEmpty();
    }

    @Test
    void listByRouteSheetWithDeliveries_anida_entregas_por_parada() {
        Delivery delivery = new Delivery(
                "del-stop-001-1", stop, "Cliente 1", "Paquete #1",
                null, null, null, null, null, DeliveryStatus.PENDING
        );
        when(deliveryRepository.findByTripStop_RouteSheet_ExternalIdOrderByTripStop_StopOrderAsc("route-demo-001"))
                .thenReturn(List.of(delivery));

        List<TripStopResponse> result = service.listByRouteSheetWithDeliveries("route-demo-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).externalId()).isEqualTo("stop-001");
        assertThat(result.get(0).deliveries()).hasSize(1);
        assertThat(result.get(0).deliveries().get(0).externalId()).isEqualTo("del-stop-001-1");
    }
}
