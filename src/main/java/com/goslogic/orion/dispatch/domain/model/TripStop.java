package com.goslogic.orion.dispatch.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_stops")
@Getter
@Setter
@NoArgsConstructor
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 100)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_sheet_id", nullable = false)
    private RouteSheet routeSheet;

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStopStatus status = TripStopStatus.PENDING;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    public TripStop(String externalId, RouteSheet routeSheet, String locationName,
                    String address, Double latitude, Double longitude,
                    Integer stopOrder, LocalDateTime estimatedArrival) {
        this.externalId = externalId;
        this.routeSheet = routeSheet;
        this.locationName = locationName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopOrder = stopOrder;
        this.estimatedArrival = estimatedArrival;
    }

    /** Registra la llegada del conductor. Idempotente si ya está ARRIVED o COMPLETED. */
    public void arrive() {
        if (status == TripStopStatus.ARRIVED || status == TripStopStatus.COMPLETED) return;
        this.status = TripStopStatus.ARRIVED;
        this.arrivalTime = LocalDateTime.now();
    }

    /** Marca la parada como completada (todas las entregas DELIVERED). */
    public void complete() {
        if (status == TripStopStatus.COMPLETED) return;
        this.status = TripStopStatus.COMPLETED;
        this.departureTime = LocalDateTime.now();
    }
}
