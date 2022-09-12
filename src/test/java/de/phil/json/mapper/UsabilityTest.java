package de.phil.json.mapper;

import de.phil.json.mapper.impl.JsonMapImpl;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shows usage of {@link JsonMap} und {@link JsonList}.
 */
public class UsabilityTest {
    private final JsonList cars = givenJsonMapWithCars();

    @Test
    void listOperations() {
        System.out.println(cars.get("name", "mercedes"));
        System.out.println(cars.get("4wd", true));
        System.out.println(cars.get("metallic", false));
    }

    @Test
    void mapOperations() {
        cars.stream()
            // is
            .filter(jm -> jm.is("metallic"))
            .filter(jm -> jm.is("color", "pale blue"))
            .filter(jm -> jm.is("color", "red", false))
            // listContains
            .filter(jm -> jm.listContains("extras", "furzkissen"))
            .filter(jm -> jm.listContains("extras", "horn", false))
            // getAsMap
            .filter(jm -> jm.getAsMap("mapOfMaps").containsKey("map1"))
            .filter(jm -> jm.getAsMap("mapOfMaps").getAsMap("map1").containsValue("C"))
            // mapContainsKey
            .filter(jm -> jm.mapContainsKey("mapOfMaps", "map1"))
            .filter(jm -> jm.mapContainsKey("mapOfMaps", "map3", false))
            // mapContainsValue
            .filter(jm -> jm.getAsMap("mapOfMaps").mapContainsValue("map2", "X"))
            .filter(jm -> jm.getAsMap("mapOfMaps").getAsMap("map2").containsValue("X"))
            .filter(jm -> jm.getAsMap("mapOfMaps").mapContainsValue("map2", "U", false))
            // getMapValue
            .filter(jm -> jm.<JsonMap>getMapValue("mapOfMaps", "map1").containsKey("a"))
            .filter(jm -> jm.getAsMap("mapOfMaps").<String>getMapValue("map1", "b").equals("B"))
            // getAs
            .filter(jm -> jm.getAs("price", Long.class) >= 10_000_000_000L)
            .forEach(System.out::println);
    }

    @SuppressWarnings({"RedundantCast", "rawtypes"})
    @Test
    void mapOperationsWithEquivalents() {
        cars.stream()
            // is
            .filter(jm -> jm.is("metallic"))
            .filter(jm -> {
                final Object metallic = jm.get("metallic");
                return metallic != null && metallic.equals(true);
            })

            .filter(jm -> jm.is("color", "pale blue"))
            .filter(jm -> {
                final Object color = jm.get("color");
                return color != null && color.equals("pale blue");
            })

            .filter(jm -> jm.is("color", "red", false))
            .filter(jm -> {
                final Object farbe1 = jm.get("color");
                return farbe1 == null || !farbe1.equals("red");
            })

            // listContains
            .filter(jm -> jm.listContains("extras", "furzkissen"))
            .filter(jm -> {
                final Object extras1 = jm.get("extras");
                return extras1 != null && ((List) extras1).contains("furzkissen");
            })

            .filter(jm -> jm.listContains("extras", "horn", false))
            .filter(jm -> {
                final Object extras = jm.get("extras");
                return extras == null || !((List) extras).contains("horn");
            })

            // getAsMap
            .filter(jm -> jm.getAsMap("mapOfMaps").getAsMap("map1").containsValue("C"))
            .filter(jm -> jm.getAsMap("mapOfMaps").containsKey("map1"))
            .filter(jm -> {
                final Object mapOfMaps2 = jm.get("mapOfMaps");
                return mapOfMaps2 != null && ((Map) mapOfMaps2).containsKey("map1");
            })

            // mapContainsKey
            .filter(jm -> jm.mapContainsKey("mapOfMaps", "map1"))
            .filter(jm -> jm.getAsMap("mapOfMaps").containsKey("map1"))
            .filter(jm -> {
                final Object mapOfMaps = jm.get("mapOfMaps");
                return mapOfMaps != null && ((Map) mapOfMaps).containsKey("map1");
            })

            .filter(jm -> jm.mapContainsKey("mapOfMaps", "map3", false))
            .filter(jm -> {
                final Object mapOfMaps1 = jm.get("mapOfMaps");
                return mapOfMaps1 == null || !((Map) mapOfMaps1).containsKey("map3");
            })

            // mapContainsValue (see mapContainsKey)
            .filter(jm -> jm.getAsMap("mapOfMaps").mapContainsValue("map2", "X"))
            .filter(jm -> jm.getAsMap("mapOfMaps").getAsMap("map2").containsValue("X"))
            .filter(jm -> jm.getAsMap("mapOfMaps").mapContainsValue("map2", "U", false))

            // getMapValue
            .filter(jm -> jm.<JsonMap>getMapValue("mapOfMaps", "map1").containsKey("a"))
            .filter(jm -> {
                final JsonMap mapOfMaps3 = (JsonMap) jm.get("mapOfMaps");
                final JsonMap map1 = (JsonMap) mapOfMaps3.get("map1");
                return map1.containsKey("a");
            })

            .filter(jm -> jm.getAsMap("mapOfMaps").<String>getMapValue("map1", "b").equals("B"))
            .filter(jm -> {
                final JsonMap mapOfMaps4 = (JsonMap) jm.get("mapOfMaps");
                final JsonMap map1 = (JsonMap) mapOfMaps4.get("map1");
                return ((String) map1.get("b")).equals("B");
            })

            // getAs
            .filter(jm -> jm.getAs("price", Long.class) >= 10_000_000_000L)
            .filter(jm -> {
                final Object price = jm.get("price");
                return price != null && ((Long) price) >= 10_000_000_000L;
            })

            .forEach(System.out::println);
    }

    @Test
    void automaticEnumConversion() {

        // No need to register converter for enums

        enum ABC {

            A("Apfel"),
            B("Banane"),
            C("Kirsche");

            final String content;
            ABC(String c) {
                this.content = c;
            }

            public String getContent() {
                return content;
            }
        }

        System.out.println(cars.get("name", "mercedes").getAsMap("mapOfMaps").getAsMap("map1").getAs("b", ABC.class).getContent());
    }

    @SneakyThrows
    private JsonList givenJsonMapWithCars() {
        final InputStream yamlStream = getClass().getResourceAsStream("/cars.yml");
        assertThat(yamlStream).isNotNull();
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMap jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        return jsonMap.getAsJsonList("cars");
    }

}
