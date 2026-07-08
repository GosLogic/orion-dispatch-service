package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.RouteSheetApplicationService;
import com.goslogic.orion.dispatch.application.exception.ForbiddenException;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.model.RouteSheetStatus;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteSheetController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
// Cobertura HTTP de escenarios de jornada, clave para concurrencia e idempotencia en sincronización móvil.
class RouteSheetControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean RouteSheetApplicationService routeSheetService;

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
    void listForDriver_retorna_200_con_hojas() throws Exception {
        when(routeSheetService.listForDriver("tenant-demo", "driver-demo"))
                .thenReturn(List.of(sheet));

        mockMvc.perform(get("/v1/dispatch/route-sheets")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "driver-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("route-demo-001"))
                .andExpect(jsonPath("$[0].tenant_id").value("tenant-demo"))
                .andExpect(jsonPath("$[0].driver_id").value("driver-demo"))
                .andExpect(jsonPath("$[0].status").value("assigned"));
    }

    @Test
    void startJornada_retorna_200() throws Exception {
        sheet.setStatus(RouteSheetStatus.IN_PROGRESS);
        when(routeSheetService.startJornada("route-demo-001", "tenant-demo", "driver-demo"))
                .thenReturn(sheet);

        mockMvc.perform(patch("/v1/dispatch/route-sheets/route-demo-001/start")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "driver-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("route-demo-001"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void endJornada_retorna_200() throws Exception {
        sheet.setStatus(RouteSheetStatus.COMPLETED);
        when(routeSheetService.endJornada("route-demo-001", "tenant-demo", "driver-demo"))
                .thenReturn(sheet);

        mockMvc.perform(patch("/v1/dispatch/route-sheets/route-demo-001/end")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "driver-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"completed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("route-demo-001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void startJornada_retorna_400_si_status_vacio() throws Exception {
        mockMvc.perform(patch("/v1/dispatch/route-sheets/route-demo-001/start")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void startJornada_retorna_404_si_no_existe() throws Exception {
        when(routeSheetService.startJornada(anyString(), anyString(), any()))
                .thenThrow(new ResourceNotFoundException("RouteSheet no encontrada: x"));

        mockMvc.perform(patch("/v1/dispatch/route-sheets/x/start")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("RouteSheet no encontrada: x"));
    }

    @Test
    void startJornada_retorna_403_si_driver_no_coincide() throws Exception {
        when(routeSheetService.startJornada(anyString(), anyString(), any()))
                .thenThrow(new ForbiddenException("El conductor del token no coincide con el asignado a la hoja de ruta"));

        mockMvc.perform(patch("/v1/dispatch/route-sheets/route-demo-001/start")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Driver-Id", "otro-driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
