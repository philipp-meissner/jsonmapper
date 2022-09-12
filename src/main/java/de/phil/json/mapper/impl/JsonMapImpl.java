// Copyright (c) by go4medical.eu 2022.

package de.phil.json.mapper.impl;

import de.phil.json.mapper.JsonMap;
import de.phil.json.mapper.JsonMapper;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Default-Implementierung.
 */
@NoArgsConstructor
public class JsonMapImpl extends HashMap<String, Object> implements JsonMap {

    @SuppressWarnings("rawtypes")
    private Class<? extends Map> optimizedForClass;

    /**
     * Erzeugt eine neue Map aus dem gegebenen JSON-String.
     *
     * @param json JSON-String.
     */
    @SuppressWarnings("unchecked")
    public JsonMapImpl(String json) {
        this(JsonMapper.readJson(json, Map.class));
    }

    /**
     * Erzeugt eine neue Map mit den gegebenen Werten.
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
