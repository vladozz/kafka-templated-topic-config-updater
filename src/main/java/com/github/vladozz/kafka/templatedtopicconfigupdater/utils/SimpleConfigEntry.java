package com.github.vladozz.kafka.templatedtopicconfigupdater.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.ConfigEntry;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleConfigEntry {
    private String name;
    private String value;
    private boolean isDefault;

    public static SimpleConfigEntry of(ConfigEntry configEntry) {
       return new SimpleConfigEntry(configEntry.name(), configEntry.value(), configEntry.isDefault());
    }

    public ConfigEntry toConfigEntry() {
        return new ConfigEntry(name, value, isDefault, false, false);
    }
}
