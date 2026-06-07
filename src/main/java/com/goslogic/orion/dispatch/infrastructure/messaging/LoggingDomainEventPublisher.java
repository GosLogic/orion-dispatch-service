package com.goslogic.orion.dispatch.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementación stub de DomainEventPublisher que registra los eventos en el log.
 * Sustituir por una implementación ActiveMQ/RabbitMQ cuando el broker esté disponible.
 */
@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(String topic, Map<String, Object> payload) {
        log.info("[EVENT] topic={} payload={}", topic, payload);
    }
}
