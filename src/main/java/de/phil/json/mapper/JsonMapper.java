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
 * Utility für den Umgang mit JSON und YAML.
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
     * Liest JSON-Daten ein und erzeugt daraus ein Objekt.
     *
     * @param json  Daten.
     * @param clazz Zu erstellende Klasse.
     * @param <T>   Typ der zu erstellenden Klasse.
     * @return Objekt mit den Daten.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static <T> T readJson(String json, Class<T> clazz) {
        return read(JSON_MAPPER, json, clazz);
    }

    /**
     * Liest YAML-Daten ein und erzeugt daraus ein Objekt.
     *
     * @param yaml  Daten.
     * @param clazz Zu erstellende Klasse.
     * @param <T>   Typ der zu erstellenden Klasse.
     * @return Objekt mit den Daten.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static <T> T readYaml(String yaml, Class<T> clazz) {
        return read(YAML_MAPPER, yaml, clazz);
    }

    /**
     * Liest einen String ein und erzeugt daraus ein Objekt.
     *
     * @param mapper Mapper für das Parsen der Daten.
     * @param source  Daten.
     * @param clazz Zu erstellende Klasse.
     * @param <T>   Typ der zu erstellenden Klasse.
     * @return Objekt mit den Daten.
     */
    @SuppressWarnings("unchecked")
    private static <T> T read(@NotNull ObjectMapper mapper, String source, Class<T> clazz) throws JsonProcessingException {
        final T t = mapper.readValue(source, clazz);
        if (!clazz.isInterface() && (t instanceof JsonMap jsonMap)) {
            jsonMap.optimize((Class<? extends JsonMap>) clazz);
        }
        return t;
    }

    /**
     * Schreibt die gegebenen Daten in einen String.
     *
     * @param data Daten.
     * @return JSON-Format.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String writeValueAsString(Object data) {
        return JSON_MAPPER.writeValueAsString(data);
    }

    /**
     * Schreibt die gegebenen Daten in einen String.
     *
     * @param data Daten.
     * @return JSON-Format.
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String writeValueAsYaml(Object data) {
        return YAML_MAPPER.writeValueAsString(data);
    }

    /**
     * Schreibt die gegebenen Daten in eine Map.
     *
     * @param data Daten.
     * @return JsonMap.
     */
    public static JsonMap writeValueAsMap(Object data) {
        return copyValue(data, JsonMapImpl.class);
    }

    /**
     * Schreibt die gegebenen Daten in eine Map.
     *
     * @param data  Daten.
     * @param clazz Zu erzeugende Klasse.
     * @param <T>   Typ der zu erzeugenden Klasse.
     * @return Spezialisierte JsonMap.
     */
    public static <T extends Map<String, Object>> T writeValueAsMap(Object data, Class<T> clazz) {
        return copyValue(data, clazz);
    }

    /**
     * Kopiert das gegebene Objekt in ein Objekt der gegebenen Klasse.
     *
     * @param data  Zu kopierendes Objekt.
     * @param clazz Klasse, in die das Objekt kopiert werden soll.
     * @param <T>   Typ des Objekts, in das die Daten kopiert werden sollen.
     * @return Kopiertes Objekt.
     */
    public static <T> T copyValue(Object data, Class<T> clazz) {
        final String json = writeValueAsString(data);
        return readJson(json, clazz);
    }

}
