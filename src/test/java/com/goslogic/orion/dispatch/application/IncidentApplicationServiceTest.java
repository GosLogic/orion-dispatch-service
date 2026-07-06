package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.domain.model.Incident;
import com.goslogic.orion.dispatch.domain.model.IncidentType;
import com.goslogic.orion.dispatch.domain.repository.IncidentRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentApplicationServiceTest {

    @Mock IncidentRepository incidentRepository;
    @Mock DomainEventPublisher eventPublisher;

    IncidentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new IncidentApplicationService(incidentRepository, eventPublisher);
    }

    private void stubSavePassthrough() {
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private IncidentApplicationService.CreateIncidentCommand cmd(boolean panic) {
        return new IncidentApplicationService.CreateIncidentCommand(
                "inc-001",
                "stop-002",
                "TRAFFIC",
                "Accidente en carril",
                "2026-06-05T11:00:00Z",
                panic,
                4.61,
                -74.08,
                "tenant-demo",
                "driver-demo"
        );
    }

    @Test
    void reportIncident_persiste_y_publica_evento_reported() {
        stubSavePassthrough();
        when(incidentRepository.findByExternalId("inc-001")).thenReturn(Optional.empty());

        Incident result = service.reportIncident(cmd(false));

        assertThat(result.getType()).isEqualTo(IncidentType.TRAFFIC);
        assertThat(result.isPanic()).isFalse();
        verify(eventPublisher).publish(eq(IncidentApplicationService.TOPIC_INCIDENT_REPORTED), any());
        verify(eventPublisher, never()).publish(eq(IncidentApplicationService.TOPIC_INCIDENT_PANIC), any());
    }

    @Test
    void reportPanic_fuerza_isPanic_y_publica_topic_panic() {
        stubSavePassthrough();
        when(incidentRepository.findByExternalId("panic-001")).thenReturn(Optional.empty());

        IncidentApplicationService.CreateIncidentCommand panicCmd =
                new IncidentApplicationService.CreateIncidentCommand(
                        "panic-001", null, "OTHER", "PÁNICO", "2026-06-05T12:00:00Z",
                        false, 4.61, -74.08, "tenant-demo", "driver-demo");

        Incident result = service.reportPanic(panicCmd);

        assertThat(result.isPanic()).isTrue();
        verify(eventPublisher).publish(eq(IncidentApplicationService.TOPIC_INCIDENT_PANIC), any());
    }

    @Test
    void reportIncident_es_idempotente_por_external_id() {
        Incident existing = new Incident(
                "inc-001", null, IncidentType.TRAFFIC, "prev", null, false,
                null, null, "tenant-demo", "driver-demo");
        when(incidentRepository.findByExternalId("inc-001")).thenReturn(Optional.of(existing));

        Incident result = service.reportIncident(cmd(false));

        assertThat(result).isSameAs(existing);
        verify(incidentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
