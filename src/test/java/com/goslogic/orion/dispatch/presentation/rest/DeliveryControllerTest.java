package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.DeliveryApplicationService;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class DeliveryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean DeliveryApplicationService deliveryService;

    Delivery delivery;

    @BeforeEach
    void setUp() {
        RouteSheet sheet = new RouteSheet(
                "route-demo-001", "tenant-demo", "driver-demo",
                "vehicle-001", "ABC-1234", "Mercedes Sprinter 2024",
                LocalDate.now()
        );
        TripStop stop = new TripStop(
                "stop-001", sheet,
                "Bodega Central", "Av. Industrial 1200",
                -12.0, -77.0, 1, null
        );
        delivery = new Delivery(
                "del-stop-001-1", stop,
                "Cliente 1", "Paquete estándar #1",
                ProofType.PHOTO, "photo.jpg", null,
                null, null, DeliveryStatus.DELIVERED
        );
    }

    @Test
    void registerDelivery_retorna_200() throws Exception {
        when(deliveryService.registerDelivery(any())).thenReturn(delivery);

        mockMvc.perform(post("/v1/dispatch/deliveries")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "del-stop-001-1",
                                  "trip_stop_id": "stop-001",
                                  "customer_name": "Cliente 1",
                                  "is_completed": true,
                                  "synced": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("del-stop-001-1"))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void registerDelivery_retorna_400_si_faltan_campos_obligatorios() throws Exception {
        mockMvc.perform(post("/v1/dispatch/deliveries")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customer_name": "Cliente 1",
                                  "is_completed": false,
                                  "synced": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void registerDelivery_retorna_404_si_stop_no_existe() throws Exception {
        when(deliveryService.registerDelivery(any()))
                .thenThrow(new ResourceNotFoundException("TripStop no encontrada: stop-xxx"));

        mockMvc.perform(post("/v1/dispatch/deliveries")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "del-new",
                                  "trip_stop_id": "stop-xxx",
                                  "is_completed": true,
                                  "synced": false
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("TripStop no encontrada: stop-xxx"));
    }
}
