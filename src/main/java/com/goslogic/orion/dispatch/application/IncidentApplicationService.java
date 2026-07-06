package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.domain.model.Incident;
import com.goslogic.orion.dispatch.domain.model.IncidentType;
import com.goslogic.orion.dispatch.domain.repository.IncidentRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class IncidentApplicationService {

    public static final String TOPIC_INCIDENT_REPORTED = "orion.incidents.reported";
    public static final String TOPIC_INCIDENT_PANIC = "orion.incidents.panic";

    private final IncidentRepository incidentRepository;
    private final DomainEventPublisher eventPublisher;

    public IncidentApplicationService(IncidentRepository incidentRepository,
                                      DomainEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    public record CreateIncidentCommand(
            String externalId,
            String stopExternalId,
            String type,
            String description,
            String reportedAt,
            boolean panic,
            Double latitude,
            Double longitude,
            String tenantExternalId,
            String driverExternalId
    ) {}

    public Incident reportIncident(CreateIncidentCommand cmd) {
        return createIncident(cmd, false);
    }

    public Incident reportPanic(CreateIncidentCommand cmd) {
        return createIncident(cmd, true);
    }

    private Incident createIncident(CreateIncidentCommand cmd, boolean forcePanic) {
        Optional<Incident> existing = incidentRepository.findByExternalId(cmd.externalId());
        if (existing.isPresent()) {
            return existing.get();
        }

        boolean isPanic = forcePanic || cmd.panic();
        IncidentType type = parseType(cmd.type());
        LocalDateTime reportedAt = parseReportedAt(cmd.reportedAt());

        Incident incident = new Incident(
                cmd.externalId(),
                cmd.stopExternalId(),
                type,
                cmd.description(),
                reportedAt,
                isPanic,
                cmd.latitude(),
                cmd.longitude(),
                cmd.tenantExternalId(),
                cmd.driverExternalId()
        );
        incidentRepository.save(incident);

        Map<String, Object> payload = Map.of(
                "external_id", incident.getExternalId(),
                "type", incident.getType().name(),
                "tenant_id", incident.getTenantExternalId(),
                "driver_id", incident.getDriverExternalId(),
                "is_panic", incident.isPanic()
        );

        if (isPanic) {
            eventPublisher.publish(TOPIC_INCIDENT_PANIC, payload);
        } else {
            eventPublisher.publish(TOPIC_INCIDENT_REPORTED, payload);
        }

        return incident;
    }

    private IncidentType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return IncidentType.OTHER;
        }
        return IncidentType.valueOf(raw.trim().toUpperCase());
    }

    private LocalDateTime parseReportedAt(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception ignored) {
            return LocalDateTime.parse(raw);
        }
    }
}
