package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStopRequest(
        @JsonProperty("location_name") @NotBlank String locationName,
        String address,
        Double latitude,
        Double longitude,
        @JsonProperty("stop_order") @NotNull Integer stopOrder
) {}
