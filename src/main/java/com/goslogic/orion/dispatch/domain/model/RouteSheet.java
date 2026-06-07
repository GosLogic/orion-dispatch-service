package com.goslogic.orion.dispatch.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_sheets")
@Getter
@Setter
@NoArgsConstructor
public class RouteSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 100)
    private String externalId;

    @Column(name = "tenant_external_id", nullable = false, length = 100)
    private String tenantExternalId;

    @Column(name = "driver_external_id", nullable = false, length = 100)
    private String driverExternalId;

    @Column(name = "vehicle_external_id", length = 100)
    private String vehicleExternalId;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Column(name = "vehicle_model", length = 100)
    private String vehicleModel;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteSheetStatus status = RouteSheetStatus.DRAFT;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RouteSheet(String externalId, String tenantExternalId, String driverExternalId,
                      String vehicleExternalId, String vehiclePlate, String vehicleModel,
                      LocalDate date) {
        this.externalId = externalId;
        this.tenantExternalId = tenantExternalId;
        this.driverExternalId = driverExternalId;
        this.vehicleExternalId = vehicleExternalId;
        this.vehiclePlate = vehiclePlate;
        this.vehicleModel = vehicleModel;
        this.date = date;
    }

    /** Cambia estado a IN_PROGRESS. Idempotente si ya está IN_PROGRESS. */
    public void start() {
        if (status == RouteSheetStatus.IN_PROGRESS) return;
        if (status == RouteSheetStatus.COMPLETED) {
            throw new IllegalStateException("No se puede iniciar una hoja ya completada");
        }
        this.status = RouteSheetStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    /** Cambia estado a COMPLETED. Idempotente si ya está COMPLETED. */
    public void complete() {
        if (status == RouteSheetStatus.COMPLETED) return;
        this.status = RouteSheetStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
