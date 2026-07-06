package com.goslogic.orion.dispatch.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Habilita JMS (ActiveMQ) para publicación de eventos de dominio vía topics.
 */
@Configuration
@EnableJms
public class JmsConfig {

    public static final String TOPIC_INCIDENT_PANIC = "orion.incidents.panic";
    public static final String TOPIC_INCIDENT_REPORTED = "orion.incidents.reported";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
