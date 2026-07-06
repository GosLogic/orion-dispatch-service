package com.goslogic.orion.dispatch.infrastructure.config;

import com.goslogic.orion.dispatch.domain.model.*;
import com.goslogic.orion.dispatch.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos demo para integración con la app móvil (movil.md §03-id-and-enum-mapping.md).
 *
 * Siembra:
 *   - Tenant: tenant-demo
 *   - Vehicle: vehicle-001 (placa ABC-1234, Mercedes Sprinter 2024)
 *   - Driver: driver-demo
 *   - RouteSheet: route-demo-001 (ASSIGNED, fecha hoy)
 *   - TripStops: stop-001 / stop-002 / stop-003
 *   - Deliveries: del-stop-001-1 / del-stop-002-1 / del-stop-003-1
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RouteSheetRepository routeSheetRepository;
    private final TripStopRepository tripStopRepository;
    private final DeliveryRepository deliveryRepository;

    public DataSeeder(RouteSheetRepository routeSheetRepository,
                      TripStopRepository tripStopRepository,
                      DeliveryRepository deliveryRepository) {
        this.routeSheetRepository = routeSheetRepository;
        this.tripStopRepository = tripStopRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoRoute();
        seedBetaRoute();
    }

    private void seedDemoRoute() {
        if (routeSheetRepository.existsByExternalId("route-demo-001")) {
            log.info("[DataSeeder] Datos demo ya existentes — omitiendo seed");
            return;
        }

        // Hoja de ruta demo
        RouteSheet sheet = new RouteSheet(
                "route-demo-001",
                "tenant-demo",
                "driver-demo",
                "vehicle-001",
                "ABC-1234",
                "Mercedes Sprinter 2024",
                LocalDate.now()
        );
        sheet.setStatus(RouteSheetStatus.ASSIGNED);
        routeSheetRepository.save(sheet);

        // Stop 1 — Bodega Central
        TripStop stop1 = new TripStop(
                "stop-001", sheet,
                "Bodega Central",
                "Av. Industrial 1200, Zona Norte",
                -12.0464, -77.0428,
                1,
                LocalDateTime.now().plusHours(1)
        );
        tripStopRepository.save(stop1);

        // Stop 2 — Supermercado El Ahorro
        TripStop stop2 = new TripStop(
                "stop-002", sheet,
                "Supermercado El Ahorro",
                "Jr. Comercio 450, Centro",
                -12.0534, -77.0500,
                2,
                LocalDateTime.now().plusHours(2)
        );
        tripStopRepository.save(stop2);

        // Stop 3 — Farmacia Salud Total
        TripStop stop3 = new TripStop(
                "stop-003", sheet,
                "Farmacia Salud Total",
                "Av. Grau 890, San Isidro",
                -12.0600, -77.0350,
                3,
                LocalDateTime.now().plusHours(3)
        );
        tripStopRepository.save(stop3);

        // Entregas (una por parada, en estado PENDING)
        deliveryRepository.save(new Delivery(
                "del-stop-001-1", stop1,
                "Cliente 1", "Paquete estándar #1",
                null, null, null, null, null,
                DeliveryStatus.PENDING
        ));
        deliveryRepository.save(new Delivery(
                "del-stop-002-1", stop2,
                "Cliente 2", "Paquete estándar #2",
                null, null, null, null, null,
                DeliveryStatus.PENDING
        ));
        deliveryRepository.save(new Delivery(
                "del-stop-003-1", stop3,
                "Cliente 3", "Paquete estándar #3",
                null, null, null, null, null,
                DeliveryStatus.PENDING
        ));

        log.info("[DataSeeder] Datos demo creados: route-demo-001 con 3 paradas y 3 entregas");
    }

    /** Hoja de ruta del segundo tenant para la demo de aislamiento multi-tenant (P0-3). Guard propio. */
    private void seedBetaRoute() {
        if (routeSheetRepository.existsByExternalId("route-beta-001")) {
            log.info("[DataSeeder] Datos beta ya existentes — omitiendo seed");
            return;
        }

        RouteSheet sheetBeta = new RouteSheet(
                "route-beta-001",
                "tenant-beta",
                "driver-beta",
                "vehicle-002",
                "XYZ-9876",
                "Iveco Daily 2025",
                LocalDate.now()
        );
        sheetBeta.setStatus(RouteSheetStatus.ASSIGNED);
        routeSheetRepository.save(sheetBeta);

        TripStop betaStop1 = new TripStop(
                "stop-beta-001", sheetBeta,
                "Centro de Distribución Beta",
                "Av. Los Álamos 300, Zona Este",
                -12.0700, -76.9800,
                1,
                LocalDateTime.now().plusHours(1)
        );
        tripStopRepository.save(betaStop1);

        TripStop betaStop2 = new TripStop(
                "stop-beta-002", sheetBeta,
                "Mercado Mayorista Beta",
                "Jr. Unión 120, Zona Este",
                -12.0750, -76.9750,
                2,
                LocalDateTime.now().plusHours(2)
        );
        tripStopRepository.save(betaStop2);

        deliveryRepository.save(new Delivery(
                "del-stop-beta-001-1", betaStop1,
                "Cliente Beta 1", "Paquete beta #1",
                null, null, null, null, null,
                DeliveryStatus.PENDING
        ));
        deliveryRepository.save(new Delivery(
                "del-stop-beta-002-1", betaStop2,
                "Cliente Beta 2", "Paquete beta #2",
                null, null, null, null, null,
                DeliveryStatus.PENDING
        ));

        log.info("[DataSeeder] Datos beta creados: route-beta-001 con 2 paradas y 2 entregas");
    }
}
