package com.goslogic.orion.dispatch.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class TripStopTest {

    RouteSheet sheet;
    TripStop stop;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void arrive_cambia_estado_a_ARRIVED() {
        stop.arrive();

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.ARRIVED);
        assertThat(stop.getArrivalTime()).isNotNull();
    }

    @Test
    void arrive_es_idempotente_si_ya_es_ARRIVED() {
        stop.arrive();
        var arrivalTime = stop.getArrivalTime();

        stop.arrive();

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.ARRIVED);
        assertThat(stop.getArrivalTime()).isEqualTo(arrivalTime);
    }

    @Test
    void arrive_no_cambia_estado_si_ya_es_COMPLETED() {
        stop.arrive();
        stop.complete();

        stop.arrive();

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.COMPLETED);
    }

    @Test
    void complete_cambia_estado_a_COMPLETED() {
        stop.arrive();
        stop.complete();

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.COMPLETED);
        assertThat(stop.getDepartureTime()).isNotNull();
    }

    @Test
    void complete_es_idempotente_si_ya_es_COMPLETED() {
        stop.arrive();
        stop.complete();
        var departureTime = stop.getDepartureTime();

        stop.complete();

        assertThat(stop.getStatus()).isEqualTo(TripStopStatus.COMPLETED);
        assertThat(stop.getDepartureTime()).isEqualTo(departureTime);
    }
}
