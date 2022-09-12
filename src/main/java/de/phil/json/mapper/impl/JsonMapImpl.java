// Copyright (c) by Philipp Meißner 2022.

package de.phil.json.mapper.impl;

import de.phil.json.mapper.JsonMap;
import de.phil.json.mapper.JsonMapping;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Default-Implementation.
 */
@NoArgsConstructor
public class JsonMapImpl extends HashMap<String, Object> implements JsonMap {

    /**
     * Class that this map is optimized for.
     */
    @SuppressWarnings("rawtypes")
    private Class<? extends Map> optimizedForClass;

    /**
     * Creates new map from JSON-String.
     *
     * @param json JSON.
     */
    @SuppressWarnings("unchecked")
    public JsonMapImpl(String json) {
        this(JsonMapping.readJson(json, Map.class));
    }

    /**
     * Creates new map from given map.
     *
     * @param values Map.
     */
    public JsonMapImpl(Map<String, Object> values) {
        super(values);
        optimize(getClass());
    }

    @Override
    public void setOptimizedFor(Class<? extends JsonMap> clazz) {
        this.optimizedForClass = clazz;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isOptimizedFor(Class<? extends Map> clazz) {
        return clazz == optimizedForClass;
    }

}
