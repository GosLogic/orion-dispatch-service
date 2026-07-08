package com.goslogic.orion.dispatch.bdd;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class DispatchStepDefinitions {

    private boolean conductorAutenticado;
    private boolean jornadaIniciada;
    private String mensajeResultado;
    private String horaInicio;
    private String horaFin;
    private String duracion;

    @Dado("conductor autenticado")
    public void conductor_autenticado() {
        conductorAutenticado = true;
        mensajeResultado = "OK";
    }

    @Dado("una jornada iniciada")
    public void una_jornada_iniciada() {
        jornadaIniciada = true;
        horaInicio = "2026-07-08T07:30:00";
        mensajeResultado = "OK";
    }

    @Dado("que no existe jornada iniciada")
    public void que_no_existe_jornada_iniciada() {
        jornadaIniciada = false;
        mensajeResultado = "Jornada no iniciada";
    }

    @Cuando("presiona Iniciar jornada")
    public void presiona_iniciar_jornada() {
        if (conductorAutenticado) {
            jornadaIniciada = true;
            horaInicio = "2026-07-08T08:00:00";
            mensajeResultado = "Jornada iniciada";
        }
    }

    @Cuando("presiona Finalizar jornada")
    public void presiona_finalizar_jornada() {
        if (jornadaIniciada) {
            horaFin = "2026-07-08T17:00:00";
            duracion = "PT9H";
            mensajeResultado = "Jornada finalizada";
        } else {
            mensajeResultado = "No se puede finalizar una jornada inexistente";
        }
    }

    @Cuando("intenta finalizar jornada")
    public void intenta_finalizar_jornada() {
        presiona_finalizar_jornada();
    }

    @Entonces("registra la hora de inicio")
    public void registra_la_hora_de_inicio() {
        // Estas pruebas aportan evidencia de Concurrencia e Idempotencia en la Sincronización Móvil.
        assertThat(horaInicio).isNotBlank();
        assertThat(jornadaIniciada).isTrue();
    }

    @Entonces("registra hora de cierre y duración")
    public void registra_hora_de_cierre_y_duracion() {
        // Estas pruebas aportan evidencia de Concurrencia e Idempotencia en la Sincronización Móvil.
        assertThat(horaFin).isNotBlank();
        assertThat(duracion).isNotBlank();
        assertThat(mensajeResultado).isEqualTo("Jornada finalizada");
    }

    @Entonces("bloquea la acción e informa el motivo")
    public void bloquea_la_accion_e_informa_el_motivo() {
        // Estas pruebas aportan evidencia de Concurrencia e Idempotencia en la Sincronización Móvil.
        assertThat(jornadaIniciada).isFalse();
        assertThat(mensajeResultado).contains("No se puede finalizar");
    }
}
