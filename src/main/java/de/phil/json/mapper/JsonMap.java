// Copyright (c) by go4medical.eu 2022.

package de.phil.json.mapper;

import de.phil.json.mapper.impl.JsonListImpl;
import de.phil.json.mapper.impl.JsonMapImpl;
import de.phil.json.typeconverter.TypeConverter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Map zum Umgang mit JSON-Formaten.
 */
public interface JsonMap extends Map<String, Object> {

    /**
     * Prüft, ob der Wert unter key true entspricht.
     * @param key Schlüssel.
     * @return Entspricht der Wert unter key dem Wert true.
     */
    default boolean is(String key) {
        return is(key, true);
    }

    /**
     * Prüft, ob der Wert unter key der gegebenen Bedingung entspricht.
     * @param key Schlüssel.
     * @param condition Bedingung.
     * @return Entspricht der Wert unter key der gegebenen Bedingung?
     */
    default boolean is(String key, boolean condition) {
        final Object value = get(key);
        return condition
                ? value != null && value.equals(true)
                : value == null || !value.equals(true);
    }

    /**
     * Vergleicht den Wert unter dem gegebenen key mit dem gegebenen Wert.
     * @param key Schlüssel.
     * @param value Wert, mit dem verglichen werden soll.
     * @return Entspricht der Wert unter key dem gegebenen Wert?
     */
    default boolean is(String key, Object value) {
        return is(key, value, true);
    }

    /**
     * Vergleicht den Wert unter dem gegebenen key mit dem gegebenen Wert.
     * @param key Schlüssel.
     * @param value Wert, mit dem verglichen werden soll.
     * @param condition Bedingung.
     * @return Entspricht der Vergleichswert der gegebenen Bedingung?
     */
    default boolean is(String key, Object value, boolean condition) {
        return condition == Objects.equals(get(key), value);
    }

    /**
     * Liest den Wert unter key als List aus und prüft, ob die Liste den gegebenen Wert enthält.
     * @param key Schlüssel.
     * @param valueInContainedList Gesuchter Wert.
     * @return this.get(key),contains(valueInContainedList).
     */
    default boolean listContains(String key, Object valueInContainedList) {
        return listContains(key, valueInContainedList, true);
    }

    /**
     * Liest den Wert unter key als List aus und prüft, ob die Liste den gegebenen Wert enthält.
     * @param key Schlüssel.
     * @param valueInContainedList Gesuchter Wert.
     * @return this.get(key),contains(valueInContainedList).
     */
    default boolean listContains(String key, Object valueInContainedList, boolean condition) {
        final List<?> list = getAs(key, List.class);
        return condition
                ? list != null && list.contains(valueInContainedList)
                : list == null || !list.contains(valueInContainedList);
    }

    /**
     * Liest den Wert unter key als Map aus und prüft, ob in der Map der gesuchte Schlüssel enthalten ist.
     * @param key Schlüssel.
     * @param keyInContainedMap Gesuchter Schlüssel.
     * @return this.get(key),containsKey(keyInContainedMap).
     */
    default boolean mapContainsKey(String key, String keyInContainedMap) {
        return mapContainsKey(key, keyInContainedMap, true);
    }

    /**
     * Liest den Wert unter key als Map aus und prüft, ob in der Map der gesuchte Schlüssel enthalten ist.
     * @param key Schlüssel.
     * @param keyInContainedMap Gesuchter Schlüssel.
     * @return this.get(key),containsKey(keyInContainedMap).
     */
    default boolean mapContainsKey(String key, String keyInContainedMap, boolean condition) {
        final JsonMap jsonMap = getAsMap(key);
        return condition == (jsonMap != null && jsonMap.containsKey(keyInContainedMap));
    }

    /**
     * Liest den Wert unter key als Map aus und prüft, ob in der Map der gesuchte Wert enthalten ist.
     * @param key Schlüssel.
     * @param valueInContainedMap Gesuchter Wert.
     * @return this.get(key),containsValue(valueInContainedMap).
     */
    default boolean mapContainsValue(String key, Object valueInContainedMap) {
        return mapContainsValue(key, valueInContainedMap, true);
    }

    /**
     * Liest den Wert unter key als Map aus und prüft, ob in der Map der gesuchte Wert enthalten ist.
     * @param key Schlüssel.
     * @param valueInContainedMap Gesuchter Wert.
     * @return this.get(key),containsValue(valueInContainedMap).
     */
    default boolean mapContainsValue(String key, Object valueInContainedMap, boolean condition) {
        final JsonMap jsonMap = getAsMap(key);
        return condition == (jsonMap != null && jsonMap.containsValue(valueInContainedMap));
    }

    /**
     * Liest den Wert unter key als Map aus und liefert Wert, der in dieser Map unter dem keyInContainedMap gespeichert ist.
     * @param key Schlüssel.
     * @param keyInContainedMap Gesuchter Schlüssel.
     * @return Wert unter this.get(key).get(keyInContainedMap)
     */
    @SuppressWarnings("unchecked")
    default <T> T getMapValue(String key, String keyInContainedMap) {
        final JsonMap jsonMap = getAsMap(key);
        return jsonMap == null
                ? null
                : (T) jsonMap.get(keyInContainedMap);
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als {@link String}.
     *
     * @param key Schlüssel.
     * @return Wert unter dem Schlüssel.
     */
    default String getAsString(String key) {
        return getAs(key, String.class);
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als {@link JsonMap}.
     *
     * @param key Schlüssel.
     * @return Wert unter dem Schlüssel.
     */
    @SuppressWarnings("UnnecessaryJavaDocLink")
    default JsonMap getAsMap(String key) {
        return getAsMap(key, JsonMapImpl.class);
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als spezialisierte {@link Map} vom gegebenen Typen.
     * Ist für die gegebene KLasse noch kein Konverter registriert, so wird automatisch ein Konverter erstellt und registriert.
     *
     * @param key   Schlüssel.
     * @param clazz Klasse des Rückgabetyps.
     * @param <T>   Typ der Rückgabe.
     * @return Wert unter dem Schlüssel.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default <T extends Map> T getAsMap(String key, Class<T> clazz) {
        if (isOptimizedFor(clazz)) {
            return (T) get(key);
        }
        TypeConverter.registerIfAbsent(clazz, value -> JsonMapper.copyValue(value, clazz));
        final T map = getAs(key, clazz);
        // konvertierte Map speichern, damit sie beim nächsten Mal nicht wieder konvertiert werden muss
        put(key, map);
        return map;
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als gegebenen Typ.
     *
     * @param key   Schlüssel.
     * @param clazz Gewünschter Rückgabetyp.
     * @param <T>   Typ der Rückgabe.
     * @return Wert unter dem Schlüssel.
     */
    default <T> T getAs(String key, Class<T> clazz) {
        return TypeConverter.convert(get(key), clazz);
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als {@link JsonList}.
     *
     * @param key Schlüssel.
     * @return Wert unter dem Schlüssel.
     */
    default JsonList getAsJsonList(String key) {
        return getAsJsonList(key, JsonMapImpl.class);
    }

    /**
     * Liefert den Wert unter dem gegebenen Schlüssel als {@link JsonList}.
     *
     * @param key   Schlüssel.
     * @param clazz Gewünschter Type der Elemente.
     * @param <T>   Typ der Elemente.
     * @return Wert unter dem Schlüssel.
     */
    default <T extends JsonMap> JsonList getAsJsonList(String key, Class<T> clazz) {
        if (isOptimizedFor(clazz)) {
            return (JsonList) get(key);
        }
        final List<?> currentValue = getAs(key, List.class);
        final JsonList listOfMaps = currentValue.stream()
                                                .map(o -> JsonMapper.writeValueAsMap(o, JsonMapImpl.class))
                                                .collect(Collectors.toCollection(JsonListImpl::new));

        // konvertierte List speichern, damit sie beim nächsten Mal nicht wieder konvertiert werden muss
        put(key, listOfMaps);
        return listOfMaps;
    }

    /**
     * Setzen, dass die JsonMap für eine bestimmte Map-Implementierung optimiert wurde.
     * Optimierung erlaubt Type-Casts statt konvertierungen.
     *
     * @param clazz Klasse, für die die JsonMap optimiert wurde.
     */
    default void setOptimizedFor(Class<? extends JsonMap> clazz) {
    }

    /**
     * Prüfen, ob di JsonMap für eine bestimmte Map-Implementierung optimiert wurde.
     * @param clazz Klasse fü die auf Optimierung geprüft werden soll.
     *
     * @return JsonMap wurde für die gegebene Klasse optimiert?
     */
    @SuppressWarnings("rawtypes")
    default boolean isOptimizedFor(Class<? extends Map> clazz){
        return false;
    }

    /**
     * JsonMap optimieren.
     * @param clazz Klasse, für die die JsonMap optimiert werden soll.
     */
    default void optimize(@NotNull Class<? extends JsonMap> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException(JsonMap.class.getSimpleName() + " cannot be optimized for interfaces");
        }
        setOptimizedFor(null);
        forEach((key, value) -> {
            if ((value instanceof Map)) {
                getAsMap(key, clazz).optimize(clazz);
            }
            if (value instanceof final List<?> list) {
                if (!list.isEmpty() && (list.get(0) instanceof Map) && (!(clazz.isAssignableFrom((list.get(0)).getClass())))) {
                    getAsJsonList(key, clazz).forEach(jm -> jm.optimize(clazz));
                }
            }
        });
        setOptimizedFor(clazz);
    }
}
