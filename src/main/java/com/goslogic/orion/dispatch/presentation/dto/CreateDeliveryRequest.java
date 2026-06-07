package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload del POST /dispatch/deliveries (campo a campo del contrato móvil — movil.md §POST /dispatch/deliveries).
 * Jackson SNAKE_CASE global resuelve la mayoría; @JsonProperty solo para casos con alias distintos.
 */
public record CreateDeliveryRequest(
        @NotBlank                    String id,
        @JsonProperty("trip_stop_id") @NotBlank String tripStopId,
        String customerName,
        String packageDescription,
        String proofType,
        String photoPath,
        String signaturePath,
        String notes,
        String deliveredAt,
        @JsonProperty("is_completed") boolean isCompleted,
        boolean synced
) {}
