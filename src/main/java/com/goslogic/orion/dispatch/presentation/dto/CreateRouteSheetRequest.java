package com.goslogic.orion.dispatch.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRouteSheetRequest(
        @JsonProperty("driver_id") @NotBlank String driverExternalId,
        @JsonProperty("vehicle_id") @NotBlank String vehicleExternalId,
        @JsonProperty("vehicle_plate") @NotBlank String vehiclePlate,
        @JsonProperty("vehicle_model") String vehicleModel,
        @JsonProperty("scheduled_date") @NotNull String scheduledDate,
        @Valid List<CreateStopRequest> stops
) {}
