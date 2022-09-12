// Copyright (c) by go4medical.eu 2022.

package de.phil.json.mapper;

import java.util.List;
import java.util.stream.Stream;

/**
 * List of {@link JsonMap}s.
 */
public interface JsonList extends List<JsonMap> {

    /**
     * Returns the map that contains the given value under the given key.
     *
     * @param key Key.
     * @param value Value to find.
     * @param <T> Type of value.
     * @return Map matching the condition.
     * @throws IllegalArgumentException no or multiple matching values found.
     */
    default <T> JsonMap get(String key, T value) throws IllegalArgumentException {
        return get(key, stream().filter(jm -> jm.is(key, value, true)));
    }

    /**
     * Returns the map that contains the given value under the given key.
     *
     * @param key  Key to find the the boolean condition under.
     * @param condition Value to find.
     * @return Map matching the condition.
     * @throws IllegalArgumentException no or multiple matching values found.
     */
    default JsonMap get(String key, boolean condition) throws IllegalArgumentException {
        return get(key, stream().filter(jm -> jm.is(key, condition)));
    }

    private JsonMap get(String key, Stream<JsonMap> content) {
        final List<JsonMap> jsonMaps = content.toList();
        return switch (jsonMaps.size()) {
            case 0 -> throw new IllegalArgumentException("key=" + key + " cannot be found");
            case 1 -> jsonMaps.get(0);
            default -> throw new IllegalArgumentException("key=" + key + " not unique");
        };
    }
}
