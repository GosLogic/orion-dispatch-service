package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.IncidentApplicationService;
import com.goslogic.orion.dispatch.domain.model.Incident;
import com.goslogic.orion.dispatch.domain.model.IncidentType;
import com.goslogic.orion.dispatch.presentation.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class IncidentControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean IncidentApplicationService incidentService;

    Incident incident;

    @BeforeEach
    void setUp() {
        incident = new Incident(
                "inc-001", "stop-002", IncidentType.TRAFFIC,
                "Accidente", LocalDateTime.now(), false,
                null, null, "tenant-demo", "driver-demo");
    }

    @Test
    void reportIncident_retorna_200() throws Exception {
        when(incidentService.reportIncident(any())).thenReturn(incident);

        mockMvc.perform(post("/v1/incidents")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "driver-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "inc-001",
                                  "stop_id": "stop-002",
                                  "type": "TRAFFIC",
                                  "description": "Accidente",
                                  "reported_at": "2026-06-05T11:00:00.000Z",
                                  "is_panic": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inc-001"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void reportPanic_retorna_200() throws Exception {
        incident.setPanic(true);
        when(incidentService.reportPanic(any())).thenReturn(incident);

        mockMvc.perform(post("/v1/incidents/panic")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "driver-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "panic-001",
                                  "type": "OTHER",
                                  "description": "ALERTA DE PÁNICO",
                                  "reported_at": "2026-06-05T12:00:00.000Z",
                                  "is_panic": true,
                                  "latitude": 4.6105,
                                  "longitude": -74.0825
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inc-001"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }
}
