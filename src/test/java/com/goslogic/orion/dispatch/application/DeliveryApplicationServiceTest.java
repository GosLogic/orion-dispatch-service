package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.DeliveryApplicationService.CreateDeliveryCommand;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.*;
import com.goslogic.orion.dispatch.domain.repository.DeliveryRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryApplicationServiceTest {

    @Mock DeliveryRepository deliveryRepository;
    @Mock TripStopRepository tripStopRepository;
    @Mock DomainEventPublisher eventPublisher;

    DeliveryApplicationService service;

    RouteSheet sheet;
    TripStop stop;

    @BeforeEach
    void setUp() {
        service = new DeliveryApplicationService(deliveryRepository, tripStopRepository, eventPublisher);

        sheet = new RouteSheet(
                "route-demo-001", "tenant-demo", "driver-demo",
                "vehicle-001", "ABC-1234", "Mercedes Sprinter 2024",
                LocalDate.now()
        );

        stop = new TripStop(
                "stop-001", sheet,
                "Bodega Central", "Av. Industrial 1200",
                -12.0, -77.0, 1, null
        );

        when(tripStopRepository.findByExternalIdAndRouteSheet_TenantExternalId("stop-001", "tenant-demo"))
                .thenReturn(Optional.of(stop));
        when(tripStopRepository.findByExternalIdAndRouteSheet_TenantExternalId("stop-xxx", "tenant-demo"))
                .thenReturn(Optional.empty());
        when(deliveryRepository.findByExternalId("del-stop-001-1")).thenReturn(Optional.empty());
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tripStopRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateDeliveryCommand cmd(String deliveryId, String stopId, boolean isCompleted) {
        return new CreateDeliveryCommand(
                deliveryId, stopId, "tenant-demo",
                "Cliente 1", "Paquete estándar #1",
                "photo", "photo_local.jpg", null,
                null, "2026-06-05T10:30:00Z", isCompleted
        );
    }

    @Test
    void registerDelivery_registra_entrega_DELIVERED() {
        Delivery result = service.registerDelivery(cmd("del-stop-001-1", "stop-001", true));
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(result.getExternalId()).isEqualTo("del-stop-001-1");
        verify(eventPublisher).publish(eq("orion.dispatch.delivery-completed"), anyMap());
    }

    @Test
    void registerDelivery_registra_entrega_PENDING_si_is_completed_false() {
        Delivery result = service.registerDelivery(cmd("del-stop-001-1", "stop-001", false));
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PENDING);
    }

    @Test
    void registerDelivery_idempotente_devuelve_existente() {
        Delivery existing = new Delivery(
                "del-stop-001-1", stop, "C1", "Pkg", ProofType.PHOTO,
                null, null, null, null, DeliveryStatus.DELIVERED
        );
        when(deliveryRepository.findByExternalId("del-stop-001-1")).thenReturn(Optional.of(existing));

        Delivery result = service.registerDelivery(cmd("del-stop-001-1", "stop-001", true));
        assertThat(result).isSameAs(existing);
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void registerDelivery_lanza_404_si_stop_no_existe() {
        assertThatThrownBy(() -> service.registerDelivery(cmd("del-xxx", "stop-xxx", true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerDelivery_marca_stop_COMPLETED_cuando_todas_las_entregas_son_DELIVERED() {
        // Si no quedan entregas pendientes → stop se completa
        when(deliveryRepository.countByTripStop_IdAndStatusNot(any(), eq(DeliveryStatus.DELIVERED)))
                .thenReturn(0L);

        service.registerDelivery(cmd("del-stop-001-1", "stop-001", true));

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.COMPLETED);
        verify(tripStopRepository).save(stop);
    }

    @Test
    void registerDelivery_no_completa_stop_si_hay_entregas_pendientes() {
        when(deliveryRepository.countByTripStop_IdAndStatusNot(any(), eq(DeliveryStatus.DELIVERED)))
                .thenReturn(2L);

        service.registerDelivery(cmd("del-stop-001-1", "stop-001", true));

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.PENDING);
        verify(tripStopRepository, never()).save(stop);
    }

    @Test
    void registerDelivery_lanza_error_si_proof_type_invalido() {
        CreateDeliveryCommand invalidCmd = new CreateDeliveryCommand(
                "del-new", "stop-001", "tenant-demo",
                "Cliente", "Paquete", "INVALIDO",
                null, null, null, null, true
        );

        assertThatThrownBy(() -> service.registerDelivery(invalidCmd))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
