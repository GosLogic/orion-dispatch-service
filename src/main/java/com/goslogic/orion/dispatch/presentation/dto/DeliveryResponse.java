package com.goslogic.orion.dispatch.presentation.dto;

import com.goslogic.orion.dispatch.domain.model.Delivery;

public record DeliveryResponse(String id, String status) {
    public static DeliveryResponse from(Delivery d) {
        return new DeliveryResponse(d.getExternalId(), d.getStatus().name());
    }
}
