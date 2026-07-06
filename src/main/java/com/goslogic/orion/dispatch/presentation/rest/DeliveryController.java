package com.goslogic.orion.dispatch.presentation.rest;

import com.goslogic.orion.dispatch.application.DeliveryApplicationService;
import com.goslogic.orion.dispatch.application.DeliveryApplicationService.CreateDeliveryCommand;
import com.goslogic.orion.dispatch.domain.model.Delivery;
import com.goslogic.orion.dispatch.presentation.dto.CreateDeliveryRequest;
import com.goslogic.orion.dispatch.presentation.dto.DeliveryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/dispatch/deliveries")
@Tag(name = "Deliveries", description = "Registro de entregas en ruta")
public class DeliveryController {

    private final DeliveryApplicationService deliveryService;

    public DeliveryController(DeliveryApplicationService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @Operation(summary = "Registrar entrega (idempotente por id)")
    public ResponseEntity<DeliveryResponse> registerDelivery(
            @Valid @RequestBody CreateDeliveryRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        CreateDeliveryCommand cmd = new CreateDeliveryCommand(
                req.id(),
                req.tripStopId(),
                tenantExternalId,
                req.customerName(),
                req.packageDescription(),
                req.proofType(),
                req.photoPath(),
                req.signaturePath(),
                req.notes(),
                req.deliveredAt(),
                req.isCompleted(),
                req.latitude(),
                req.longitude()
        );
        Delivery delivery = deliveryService.registerDelivery(cmd);
        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }
}
