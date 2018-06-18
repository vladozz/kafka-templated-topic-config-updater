package com.github.vladozz.kafka.templatedtopicconfigupdater.utils;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MapCollectors {
    public static <T extends Map.Entry<K, U>, K, U> Collector<T, ?, Map<K, U>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
