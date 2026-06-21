package com.goslogic.orion.dispatch.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class RouteSheetTest {

    RouteSheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new RouteSheet(
                "route-demo-001", "tenant-demo", "driver-demo",
                "vehicle-001", "ABC-1234", "Mercedes Sprinter 2024",
                LocalDate.now()
        );
        sheet.setStatus(RouteSheetStatus.ASSIGNED);
    }

    @Test
    void start_cambia_estado_a_IN_PROGRESS() {
        sheet.start();

        assertThat(sheet.getStatus()).isEqualTo(RouteSheetStatus.IN_PROGRESS);
        assertThat(sheet.getStartedAt()).isNotNull();
    }

    @Test
    void start_es_idempotente_si_ya_es_IN_PROGRESS() {
        sheet.start();
        var startedAt = sheet.getStartedAt();

        sheet.start();

        assertThat(sheet.getStatus()).isEqualTo(RouteSheetStatus.IN_PROGRESS);
        assertThat(sheet.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    void start_lanza_excepcion_si_ya_esta_COMPLETED() {
        sheet.start();
        sheet.complete();

        assertThatThrownBy(sheet::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede iniciar una hoja ya completada");
    }

    @Test
    void complete_cambia_estado_a_COMPLETED() {
        sheet.start();
        sheet.complete();

        assertThat(sheet.getStatus()).isEqualTo(RouteSheetStatus.COMPLETED);
        assertThat(sheet.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_es_idempotente_si_ya_es_COMPLETED() {
        sheet.start();
        sheet.complete();
        var completedAt = sheet.getCompletedAt();

        sheet.complete();

        assertThat(sheet.getStatus()).isEqualTo(RouteSheetStatus.COMPLETED);
        assertThat(sheet.getCompletedAt()).isEqualTo(completedAt);
    }
}
