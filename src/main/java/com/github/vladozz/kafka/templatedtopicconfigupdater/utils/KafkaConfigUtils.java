package com.github.vladozz.kafka.templatedtopicconfigupdater.utils;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class KafkaConfigUtils {
    public static boolean isEqual(Collection<ConfigEntry> configEntries1, Collection<ConfigEntry> configEntries2) {
        return toSimpleConfigEntries(configEntries1).equals(toSimpleConfigEntries(configEntries2));
    }

    public static Object toSimpleConfigEntries(Collection<ConfigEntry> configEntries1) {
        return configEntries1.stream()
                .map(SimpleConfigEntry::of).collect(Collectors.toSet());
    }

    public static List<ConfigEntry> filterOverridden(Collection<ConfigEntry> entries) {
        return entries.stream()
                .filter(configEntry -> !configEntry.isDefault())
                .collect(toList());
    }

    public static ConfigResource topicConfigResource(String name) {
        return new ConfigResource(ConfigResource.Type.TOPIC, name);
    }
}
