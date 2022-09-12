// Copyright (c) by go4medical.eu 2022.

package de.phil.json.mapper;

import java.util.List;
import java.util.stream.Stream;

/**
 * Liste von {@link JsonMap}s.
 */
public interface JsonList extends List<JsonMap> {

    /**
     * Liefert die Map, in der der Wert unter key dem gegebenen Wert entspricht.
     *
     * @param key Schlüssel.
     * @param value Wert.
     * @param <T> Typ des Werts.
     * @return Map, die den Bedingungen entspricht.
     * @throws IllegalArgumentException kein passender Wert gefunden oder Wert nicht eindeutig.
     */
    default <T> JsonMap get(String key, T value) throws IllegalArgumentException {
        return get(key, stream().filter(jm -> jm.is(key, value, true)));
    }

    /**
     * Liefert die Map, in der der Bool'sche Wert unter key der gegebenen Bedingung entspricht.
     *
     * @param key       Schlüssel, unter dem der Bool'sche Wert abgelegt ist.
     * @param condition Bedingung, der der Wert entsprechen muss.
     * @return Stream der Maps, die den Bedingungen entsprechen.
     * @throws IllegalArgumentException kein passender Wert gefunden oder Wert nicht eindeutig.
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
