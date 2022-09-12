// Copyright (c) by go4medical.eu 2022.

package de.phil.json.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import de.phil.json.mapper.impl.JsonMapImpl;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Utility for dealing with JSON and YAML.
 */
public class JsonMapper {

    private static final YAMLFactory YAML_FACTORY = new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                                                                     .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
                                                                     .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER= new ObjectMapper(YAML_FACTORY);

    static {
        configure(JSON_MAPPER);
        configure(YAML_MAPPER);
    }

    private static void configure(@NotNull ObjectMapper mapper) {
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Reads JSON and creates object.
     *
     * @param json  JSON data
     * @param clazz Class to instantiate.
     * @param <T>   Type of class.
     * @return Object containing JSON data.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static <T> T readJson(String json, Class<T> clazz) {
        return read(JSON_MAPPER, json, clazz);
    }

    /**
     * Reads YAML and creates object.
     *
     * @param yaml  YAML data
     * @param clazz Class to instantiate.
     * @param <T>   Type of class.
     * @return Object containing YAML data.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static <T> T readYaml(String yaml, Class<T> clazz) {
        return read(YAML_MAPPER, yaml, clazz);
    }

    @SuppressWarnings("unchecked")
    private static <T> T read(@NotNull ObjectMapper mapper, String source, Class<T> clazz) throws JsonProcessingException {
        final T t = mapper.readValue(source, clazz);
        if (!clazz.isInterface() && (t instanceof JsonMap jsonMap)) {
            jsonMap.optimize((Class<? extends JsonMap>) clazz);
        }
        return t;
    }

    /**
     * Writes object to JSON String.
     *
     * @param data Data.
     * @return JSON.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String writeValueAsString(Object data) {
        return JSON_MAPPER.writeValueAsString(data);
    }

    /**
     * Writes object to YAML String.
     *
     * @param data Data.
     * @return YAML.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String writeValueAsYaml(Object data) {
        return YAML_MAPPER.writeValueAsString(data);
    }

    /**
     * Writes data to {@link JsonMap}.
     *
     * @param data data.
     * @return JsonMap.
     */
    public static JsonMap writeValueAsMap(Object data) {
        return copyValue(data, JsonMapImpl.class);
    }

    /**
     * Writes data to {@link JsonMap}.
     *
     * @param data data.
     * @param clazz Class of JsonMap.
     * @param <T> JsonMap-type.
     * @return JsonMap.
     */
    public static <T extends Map<String, Object>> T writeValueAsMap(Object data, Class<T> clazz) {
        return copyValue(data, clazz);
    }

    /**
     * Copies one object to another.
     *
     * @param data  Object to copy.
     * @param clazz Class to copy object to.
     * @param <T>   Type of object to copy to.
     * @return Copy.
     */
    public static <T> T copyValue(Object data, Class<T> clazz) {
        final String json = writeValueAsString(data);
        return readJson(json, clazz);
    }

}
