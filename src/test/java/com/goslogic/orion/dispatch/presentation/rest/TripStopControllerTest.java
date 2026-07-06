package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.TripStopApplicationService;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.RouteSheet;
import com.goslogic.orion.dispatch.domain.model.TripStop;
import com.goslogic.orion.dispatch.domain.model.TripStopStatus;
import com.goslogic.orion.dispatch.presentation.dto.DeliverySummaryResponse;
import com.goslogic.orion.dispatch.presentation.dto.TripStopResponse;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripStopController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class TripStopControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TripStopApplicationService tripStopService;

    TripStop stop;

    @BeforeEach
    void setUp() {
        RouteSheet sheet = new RouteSheet(
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
    void listByRouteSheet_retorna_200() throws Exception {
        TripStopResponse response = TripStopResponse.from(stop, List.of());
        when(tripStopService.listByRouteSheetWithDeliveries("route-demo-001")).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/dispatch/trip-stops")
                        .param("route_sheet_id", "route-demo-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("stop-001"))
                .andExpect(jsonPath("$[0].route_sheet_id").value("route-demo-001"))
                .andExpect(jsonPath("$[0].location_name").value("Bodega Central"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].deliveries").isArray())
                .andExpect(jsonPath("$[0].deliveries").isEmpty());
    }

    @Test
    void listByRouteSheet_retorna_entregas_anidadas() throws Exception {
        DeliverySummaryResponse delivery = new DeliverySummaryResponse(
                "del-stop-001-1", "stop-001", "Cliente 1", "Paquete #1",
                "PENDING", null, null, null, null, null, null, null
        );
        TripStopResponse response = TripStopResponse.from(stop, List.of(delivery));
        when(tripStopService.listByRouteSheetWithDeliveries("route-demo-001")).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/dispatch/trip-stops")
                        .param("route_sheet_id", "route-demo-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deliveries[0].id").value("del-stop-001-1"))
                .andExpect(jsonPath("$[0].deliveries[0].customer_name").value("Cliente 1"));
    }

    @Test
    void listByRouteSheet_retorna_lista_vacia() throws Exception {
        when(tripStopService.listByRouteSheetWithDeliveries("route-vacia")).thenReturn(List.of());

        mockMvc.perform(get("/v1/dispatch/trip-stops")
                        .param("route_sheet_id", "route-vacia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void markArrived_retorna_200() throws Exception {
        stop.setStatus(TripStopStatus.ARRIVED);
        when(tripStopService.markArrived(eq("stop-001"), eq("tenant-demo"), any(), any())).thenReturn(stop);

        mockMvc.perform(patch("/v1/dispatch/trip-stops/stop-001/arrived")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"arrived\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("stop-001"))
                .andExpect(jsonPath("$.status").value("ARRIVED"));
    }

    @Test
    void markArrived_retorna_400_si_status_vacio() throws Exception {
        mockMvc.perform(patch("/v1/dispatch/trip-stops/stop-001/arrived")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void markArrived_retorna_404_si_no_existe() throws Exception {
        when(tripStopService.markArrived(anyString(), anyString(), any(), any()))
                .thenThrow(new ResourceNotFoundException("TripStop no encontrada: stop-xxx"));

        mockMvc.perform(patch("/v1/dispatch/trip-stops/stop-xxx/arrived")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"arrived\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("TripStop no encontrada: stop-xxx"));
    }
}
