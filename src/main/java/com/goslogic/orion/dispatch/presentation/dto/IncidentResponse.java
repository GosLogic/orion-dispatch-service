package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.dispatch.domain.model.Incident;

public record IncidentResponse(
        @JsonProperty("id") String externalId,
        String status
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(incident.getExternalId(), incident.getStatus().name());
    }
}
