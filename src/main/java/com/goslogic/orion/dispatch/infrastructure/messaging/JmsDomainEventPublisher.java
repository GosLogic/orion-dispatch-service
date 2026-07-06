package com.goslogic.orion.dispatch.infrastructure.messaging;

import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Publica eventos de dominio al broker JMS/ActiveMQ (topics).
 * Bean @Primary; {@link LoggingDomainEventPublisher} queda como fallback en tests.
 */
@Component
@Primary
public class JmsDomainEventPublisher implements DomainEventPublisher {

    private final JmsTemplate jmsTemplate;

    public JmsDomainEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void publish(String topic, Map<String, Object> payload) {
        jmsTemplate.convertAndSend(topic, payload);
    }
}
