package com.github.vladozz.kafka.templatedtopicconfigupdater.service;

import com.github.vladozz.kafka.templatedtopicconfigupdater.exception.TemplateTopicNotFoundException;
import com.github.vladozz.kafka.templatedtopicconfigupdater.properties.KafkaTemplatedConfigUpdaterProperties;
import com.github.vladozz.kafka.templatedtopicconfigupdater.utils.KafkaConfigUtils;
import com.github.vladozz.kafka.templatedtopicconfigupdater.utils.MapCollectors;
import javaslang.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static javaslang.Predicates.is;
import static javaslang.Predicates.noneOf;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTemplatedConfigUpdaterService {

    private final AdminClient adminClient;
    private final KafkaTemplatedConfigUpdaterProperties properties;

    public void update() {
        Collection<String> allTopics = getTopics();
        if (!allTopics.contains(properties.getTemplateTopic())) {
            throw new TemplateTopicNotFoundException(properties.getTemplateTopic());
        }
        List<ConfigEntry> templateOverriddenConfig = getTemplateOverriddenConfig();

        Collection<String> matchingTopics = getMatchingTopics(allTopics);
        Map<String, List<ConfigEntry>> topicsOverriddenConfig = getOverriddenConfig(matchingTopics);
        List<String> topicsWithOutdatedConfig = getTopicsWithOutdatedConfig(
                templateOverriddenConfig, topicsOverriddenConfig);

        updateTopicsConfig(topicsWithOutdatedConfig, templateOverriddenConfig);
    }

    private Collection<String> getTopics() {
        return Try.of(() -> adminClient.listTopics().names().get())
                .getOrElseThrow(e -> new RuntimeException("Could not get list of topics", e));
    }

    private List<ConfigEntry> getTemplateOverriddenConfig() {
        DescribeConfigsResult templateConfigResult =
                adminClient.describeConfigs(
                        singleton(KafkaConfigUtils.topicConfigResource(properties.getTemplateTopic())));

        Config config = Try.of(() ->
                templateConfigResult.values()
                        .get(KafkaConfigUtils.topicConfigResource(
                                properties.getTemplateTopic()))
                        .get())
                .getOrElseThrow(e -> new RuntimeException("Could not get configuration of template topic", e));
        return KafkaConfigUtils.filterOverridden(config.entries());
    }

    @SneakyThrows
    private Collection<String> getMatchingTopics(Collection<String> topics) {
        List<Predicate<String>> topicRegexps = properties.getRegexp()
                .stream()
                .map(Pattern::compile)
                .map(Pattern::asPredicate)
                .collect(toList());

        return topics.stream()
                .filter(topic -> topicRegexps.stream()
                        .anyMatch(predicate -> predicate.test(topic)))
                .filter(noneOf(is(properties.getTemplateTopic())))
                .collect(toList());
    }

    private Map<String, List<ConfigEntry>> getOverriddenConfig(Collection<String> matchingTopics) {
        DescribeConfigsResult topicsConfigsResult = adminClient.describeConfigs(
                matchingTopics.stream()
                        .map(KafkaConfigUtils::topicConfigResource)
                        .collect(toList()));

        Map<ConfigResource, Config> configResourceConfigMap = Try.of(() ->
                topicsConfigsResult
                        .all().get())
                .getOrElseThrow(e -> new RuntimeException("Could not get topics configuration", e));

        return configResourceConfigMap
                .entrySet()
                .stream()
                .map(configResourceConfigEntry ->
                        Pair.of(
                                configResourceConfigEntry.getKey().name(),
                                KafkaConfigUtils.filterOverridden(
                                        configResourceConfigEntry.getValue()
                                                .entries())))
                .collect(MapCollectors.toMap());
    }

    private List<String> getTopicsWithOutdatedConfig(List<ConfigEntry> templateConfig,
                                                     Map<String, List<ConfigEntry>> topicsConfig) {
        return topicsConfig.entrySet()
                .stream()
                .filter(configResourceConfigEntry ->
                        !KafkaConfigUtils.isEqual(
                                templateConfig,
                                configResourceConfigEntry.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .collect(toList());
    }

    @SneakyThrows
    private void updateTopicsConfig(Collection<String> topics, Collection<ConfigEntry> configEntries) {
        if (isNotEmpty(topics)) {
            Map<ConfigResource, Config> configMap =
                    topics.stream().collect(Collectors.toMap(
                            KafkaConfigUtils::topicConfigResource,
                            (o) -> new Config(configEntries)));

            adminClient
                    .alterConfigs(configMap)
                    .all()
                    .get();

            log.info("Topics {} configuration have been updated with: {}. Template topic: {}",
                    topics, configEntries, properties.getTemplateTopic());
        } else {
            log.info("All topic are up-to-date with template topic {}", properties.getTemplateTopic());
        }
    }
}
