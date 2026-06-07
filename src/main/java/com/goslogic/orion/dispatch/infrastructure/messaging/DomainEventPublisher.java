package com.goslogic.orion.dispatch.infrastructure.messaging;

import java.util.Map;

public interface DomainEventPublisher {

    /** Publica un evento de dominio en el topic indicado con el payload dado. */
    void publish(String topic, Map<String, Object> payload);
}
