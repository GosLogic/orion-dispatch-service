package com.goslogic.orion.dispatch.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 100)
    private String externalId;

    @Column(name = "stop_external_id", length = 100)
    private String stopExternalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "is_panic", nullable = false)
    private boolean panic;

    private Double latitude;

    private Double longitude;

    @Column(name = "tenant_external_id", nullable = false, length = 100)
    private String tenantExternalId;

    @Column(name = "driver_external_id", nullable = false, length = 100)
    private String driverExternalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.RECEIVED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Incident(String externalId,
                    String stopExternalId,
                    IncidentType type,
                    String description,
                    LocalDateTime reportedAt,
                    boolean panic,
                    Double latitude,
                    Double longitude,
                    String tenantExternalId,
                    String driverExternalId) {
        this.externalId = externalId;
        this.stopExternalId = stopExternalId;
        this.type = type;
        this.description = description;
        this.reportedAt = reportedAt;
        this.panic = panic;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tenantExternalId = tenantExternalId;
        this.driverExternalId = driverExternalId;
    }
}
