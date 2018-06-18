package com.github.vladozz.kafka.templatedtopicconfigupdater.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("kafka-templated-config-updater")
public class KafkaTemplatedConfigUpdaterProperties {

    @NotNull
    @Size(min = 1)
    private String templateTopic;

    @NotNull
    @Size(min = 1)
    private List<String> regexp;
}
