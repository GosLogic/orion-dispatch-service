package com.goslogic.orion.dispatch.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
        @NotBlank String status,
        Double latitude,
        Double longitude
) {}
