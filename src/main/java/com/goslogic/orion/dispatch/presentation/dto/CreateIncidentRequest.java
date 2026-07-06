package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload móvil POST /incidents y POST /incidents/panic (movil.md).
 */
public record CreateIncidentRequest(
        @JsonProperty("id") String externalId,
        @JsonProperty("stop_id") String stopExternalId,
        String type,
        String description,
        @JsonProperty("reported_at") String reportedAt,
        @JsonProperty("is_panic") Boolean panic,
        Double latitude,
        Double longitude,
        Boolean synced
) {}
