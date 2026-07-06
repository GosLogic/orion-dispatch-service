package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.dispatch.domain.model.Delivery;

/**
 * Entrega anidada en GET /dispatch/trip-stops — misma forma que el POST móvil (solo lectura).
 */
public record DeliverySummaryResponse(
        @JsonProperty("id") String externalId,
        @JsonProperty("trip_stop_id") String tripStopExternalId,
        @JsonProperty("customer_name") String customerName,
        @JsonProperty("package_description") String packageDescription,
        String status,
        @JsonProperty("proof_type") String proofType,
        @JsonProperty("photo_path") String photoPath,
        @JsonProperty("signature_path") String signaturePath,
        String notes,
        @JsonProperty("delivered_at") String deliveredAt,
        Double latitude,
        Double longitude
) {
    public static DeliverySummaryResponse from(Delivery delivery) {
        return new DeliverySummaryResponse(
                delivery.getExternalId(),
                delivery.getTripStop().getExternalId(),
                delivery.getCustomerName(),
                delivery.getDescription(),
                delivery.getStatus().name(),
                delivery.getProofType() != null ? delivery.getProofType().name().toLowerCase() : null,
                delivery.getPhotoUrl(),
                delivery.getSignatureUrl(),
                delivery.getNotes(),
                delivery.getDeliveredAt() != null ? delivery.getDeliveredAt().toString() : null,
                delivery.getLatitude(),
                delivery.getLongitude()
        );
    }
}
