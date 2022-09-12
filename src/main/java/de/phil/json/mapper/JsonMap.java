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
 * Map to deal JSON format.
 */
public interface JsonMap extends Map<String, Object> {

    /**
     * Checks whether value under key is true.
     * @param key key.
     * @return true if value matches condition.
     */
    default boolean is(String key) {
        return is(key, true);
    }

    /**
     * Checks whether value under key matches condition.
     * @param key key.
     * @param condition condition.
     * @return true if value matches condition.
     */
    default boolean is(String key, boolean condition) {
        final Object value = get(key);
        return condition
                ? value != null && value.equals(true)
                : value == null || !value.equals(true);
    }

    /**
     * Compares value under given key with the given value.
     * @param key key.
     * @param value Value to compare to.
     * @return true if value matches condition.
     */
    default boolean is(String key, Object value) {
        return is(key, value, true);
    }

    /**
     * Compares value under given key with value.
     * @param key key.
     * @param value Value to compare to.
     * @param condition condition.
     * @return true if comparison matches condition.
     */
    default boolean is(String key, Object value, boolean condition) {
        return condition == Objects.equals(get(key), value);
    }

    /**
     * Checks whether the list stored under given key contains given value.
     * @param key key
     * @param valueInContainedList list value in question.
     * @return <code>this.get(key).contains(valueInContainedList)</code>.
     */
    default boolean listContains(String key, Object valueInContainedList) {
        return listContains(key, valueInContainedList, true);
    }

    /**
     * Checks whether the list stored under given key contains given value.
     * @param key key
     * @param valueInContainedList list value in question.
     * @param condition Bedingung.
     * @return <code>condition==this.get(key).contains(valueInContainedList)</code>.
     */
    default boolean listContains(String key, Object valueInContainedList, boolean condition) {
        final List<?> list = getAs(key, List.class);
        return condition
                ? list != null && list.contains(valueInContainedList)
                : list == null || !list.contains(valueInContainedList);
    }

    /**
     * Checks whether map under given key contains given key.
     * @param key key.
     * @param keyInContainedMap key in contained map.
     * @return <code>this.get(key).containsKey(keyInContainedMap)</code>.
     */
    default boolean mapContainsKey(String key, String keyInContainedMap) {
        return mapContainsKey(key, keyInContainedMap, true);
    }

    /**
     * Checks whether map under given key contains given key.
     * @param key key.
     * @param keyInContainedMap key in contained map.
     * @param condition condition.
     * @return <code>condition==this.get(key).containsKey(keyInContainedMap)</code>.
     */
    default boolean mapContainsKey(String key, String keyInContainedMap, boolean condition) {
        final JsonMap jsonMap = getAsMap(key);
        return condition == (jsonMap != null && jsonMap.containsKey(keyInContainedMap));
    }

    /**
     * Checks whether map under given key contains given value.
     * @param key key.
     * @param valueInContainedMap value in contained map.
     * @return <code>this.get(key).containsValue(valueInContainedMap)</code>.
     */
    default boolean mapContainsValue(String key, Object valueInContainedMap) {
        return mapContainsValue(key, valueInContainedMap, true);
    }

    /**
     * Checks whether map under given key contains given value.
     * @param key key.
     * @param valueInContainedMap value in contained map.
     * @param condition condition.
     * @return <code>condition==this.get(key).containsValue(valueInContainedMap)</code>.
     */
    default boolean mapContainsValue(String key, Object valueInContainedMap, boolean condition) {
        final JsonMap jsonMap = getAsMap(key);
        return condition == (jsonMap != null && jsonMap.containsValue(valueInContainedMap));
    }

    /**
     * Reads value under given as a map and returns the value stored in sub-map.
     * @param key key.
     * @param keyInContainedMap key in contained map.
     * @param <T> type of the value in the sub-map.
     * @return <code>(T) this.get(key).get(keyInContainedMap)</code>)
     */
    @SuppressWarnings("unchecked")
    default <T> T getMapValue(String key, String keyInContainedMap) {
        final JsonMap jsonMap = getAsMap(key);
        return jsonMap == null
                ? null
                : (T) jsonMap.get(keyInContainedMap);
    }

    /**
     * Gets value under given key as a String.
     * @param key key.
     * @return value under key.
     */
    default String getAsString(String key) {
        return getAs(key, String.class);
    }

    /**
     * Gets value under given key as a map.
     * @param key key.
     * @return value under key.
     */
    default JsonMap getAsMap(String key) {
        return getAsMap(key, JsonMapImpl.class);
    }

    /**
     * Gets value under given key as a map.
     * @param key key.
     * @param clazz Class of map.
     * @param <T> map-type.
     * @return value under key.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default <T extends Map> T getAsMap(String key, Class<T> clazz) {
        if (isOptimizedFor(clazz)) {
            return (T) get(key);
        }
        TypeConverter.registerIfAbsent(clazz, value -> JsonMapping.copyValue(value, clazz));
        final T map = getAs(key, clazz);
        // store converted map so next time we can cast and don't need to convert
        put(key, map);
        return map;
    }

    /**
     * Gets value under given key as the given class.
     * @param key key.
     * @param clazz Class to convert to.
     * @param <T> type.
     * @return value under key.
     */
    default <T> T getAs(String key, Class<T> clazz) {
        return TypeConverter.convert(get(key), clazz);
    }

    /**
     * Gets value under given key as {@link JsonList}.
     *
     * @param key key.
     * @return value under key.
     */
    default JsonList getAsJsonList(String key) {
        return getAsJsonList(key, JsonMapImpl.class);
    }

    /**
     * Gets value under given key as {@link JsonList} of given map-class.
     *
     * @param key key.
     * @param clazz Class of map-types.
     * @param <T> map-type.
     * @return value under key.
     */
    default <T extends JsonMap> JsonList getAsJsonList(String key, Class<T> clazz) {
        if (isOptimizedFor(clazz)) {
            return (JsonList) get(key);
        }
        final List<?> currentValue = getAs(key, List.class);
        final JsonList listOfMaps = currentValue.stream()
                                                .map(o -> JsonMapping.writeValueAsMap(o, JsonMapImpl.class))
                                                .collect(Collectors.toCollection(JsonListImpl::new));

        // store converted list so next time we can cast and don't need to convert
        put(key, listOfMaps);
        return listOfMaps;
    }

    /**
     * Sets the class map is optimized for.
     *
     * @param clazz Class for optimization.
     */
    default void setOptimizedFor(Class<? extends JsonMap> clazz) {
    }

    /**
     * Checks whether map is optimized for given class.
     * @param clazz Class for optimization.
     *
     * @return true if map is optimized for the given class.
     */
    @SuppressWarnings("rawtypes")
    default boolean isOptimizedFor(Class<? extends Map> clazz){
        return false;
    }

    /**
     * Optimize map. Optimization allows class-casts instead of transformations of map- and list-types.
     * @param clazz Class for optimization.
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
