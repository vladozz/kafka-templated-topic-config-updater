package com.github.vladozz.kafka.templatedtopicconfigupdater.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaAdminClientConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(
                kafkaProperties.buildAdminProperties());
    }
}
