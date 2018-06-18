package com.github.vladozz.kafka.templatedtopicconfigupdater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import com.github.vladozz.kafka.templatedtopicconfigupdater.properties.KafkaTemplatedConfigUpdaterProperties;
import com.github.vladozz.kafka.templatedtopicconfigupdater.service.KafkaTemplatedConfigUpdaterService;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties(KafkaTemplatedConfigUpdaterProperties.class)
public class Application implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private KafkaTemplatedConfigUpdaterService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.update();
    }
}
