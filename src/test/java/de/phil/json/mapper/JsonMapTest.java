package de.phil.json.mapper;

import de.phil.json.mapper.impl.JsonMapImpl;
import de.phil.json.typeconverter.TypeConversionException;
import de.phil.json.typeconverter.TypeConverter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JsonMap}.
 */
class JsonMapTest {
    private static final String NAME = "Hans Dietrich Genscher";
    private static final Instant MEMBER_SINCE = Instant.now();

    private final JsonList cars = givenJsonMapWithCars();

    @Test
    void canGetMap() {
        final Map<String, Person> personMap = Collections.singletonMap("person", new Person(NAME, MEMBER_SINCE));
        final JsonMap jsonMap = JsonMapper.writeValueAsMap(personMap);
        final JsonMap person = jsonMap.getAsMap("person");
        assertThat(person.getAsString(Person.Fields.name)).isEqualTo(NAME);
        assertThat(Instant.parse(person.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
    }

    @Test
    void entryInMapCanBeModified() {
        final Map<String, Person> personMap = Collections.singletonMap("person", new Person(NAME, MEMBER_SINCE));
        final JsonMap jsonMap = JsonMapper.writeValueAsMap(personMap);
        final JsonMap person = jsonMap.getAsMap("person");
        assertThat(person.getAsString(Person.Fields.name)).isEqualTo(NAME);
        person.put(Person.Fields.name, "Egon Müller");
        assertThat(jsonMap.getAsMap("person").getAsString(Person.Fields.name)).isEqualTo("Egon Müller");
    }

    @Test
    void mapGottenTwiceIsTheSameMap() {
        final Map<String, Person> personMap = Collections.singletonMap("person", new Person(NAME, MEMBER_SINCE));
        final JsonMap jsonMap = JsonMapper.writeValueAsMap(personMap);
        final JsonMap person1 = jsonMap.getAsMap("person");
        final JsonMap person2 = jsonMap.getAsMap("person");
        assertThat(person2).isSameAs(person1);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void entryInlistOfMapsCanBeModified() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMap jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        final JsonList persons = jsonMap.getAsJsonList("persons");
        persons.get(0).put("x", "y");
        assertThat(jsonMap.getAsJsonList("persons").get(0).getAsString("x")).isEqualTo("y");
        persons.add(new JsonMapImpl(Map.of("a", "b")));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void listEntryInlistOfMapsCanBeAdded() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        final JsonList persons = jsonMap.getAsJsonList("persons", JsonMapImpl.class);
        persons.add(new JsonMapImpl(Map.of("x", "x")));
        assertThat(jsonMap.getAsJsonList("persons")).hasSize(3);
        assertThat(jsonMap.getAsJsonList("persons", JsonMapImpl.class)).hasSize(3);
    }

    @Test
    void canGetSpecificMap() {
        final Map<String, Person> personMap = Collections.singletonMap("person", new Person(NAME, MEMBER_SINCE));
        final JsonMap jsonMap = JsonMapper.writeValueAsMap(personMap);
        final MemberJsonMap person = jsonMap.getAsMap("person", MemberJsonMap.class);
        assertThat(person.getAsString(Person.Fields.name)).isEqualTo(NAME);
        assertThat(person.getName()).isEqualTo(NAME);

        assertThat(Instant.parse(person.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
        assertThat(person.getMemberSince()).isEqualTo(MEMBER_SINCE);
    }


    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetAs() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234}");
        assertThat(map.getAs("a", Integer.class)).isEqualTo(1234);
        assertThat(map.getAs("a", Long.class)).isEqualTo(1234L);
        assertThat(map.getAs("a", Double.class)).isEqualTo(1234D);
        assertThat(map.getAs("a", BigInteger.class)).isEqualTo(new BigInteger("1234"));
        assertThat(map.getAs("a", BigDecimal.class)).isEqualTo(new BigDecimal("1234"));
        final Date d = new Date();
        assertThatThrownBy(() -> map.getAs("a", Date.class))
                  .isInstanceOf(TypeConversionException.class)
                  .hasMessageContaining("value=1234 cannot be converted to " + Date.class);
        TypeConverter.register(Date.class, value -> d);
        assertThat(map.getAs("a", Date.class)).isEqualTo(d);
    }

    private static Stream<Class<?>> registeredConverters() {
        return TypeConverter.getRegistrations()
                            .stream()
                            .filter(c -> !String.class.isAssignableFrom(c))
                            .filter(c -> !Map.class.isAssignableFrom(c))
                            .filter(c -> !Date.class.isAssignableFrom(c))
                            .sorted(Comparator.comparing(Class::getName));
    }

    @ParameterizedTest
    @MethodSource("registeredConverters")
    void throwsExpectedExceptionOnIllegalConversion(Class<?> clazz) {
        assertThatThrownBy(() -> {
            final Map<String, Person> personMap = Collections.singletonMap("person", new Person(NAME, MEMBER_SINCE));
            final JsonMap jsonMap = JsonMapper.writeValueAsMap(personMap);
            jsonMap.getAs("person", clazz);
        }).isInstanceOf(TypeConversionException.class)
                  .hasMessageContaining(JsonMapImpl.class + " with value=")
                  .hasMessageContaining(" cannot be converted to " + clazz);
    }

    @Test
    void throwsExpectedExceptionOnMissingConverter() {
        assertThatThrownBy(() -> {
            final JsonMapImpl map = new JsonMapImpl("{\"a\": \"true\"}");
            map.getAs("a", InputStream.class);
        }).isInstanceOf(TypeConversionException.class)
                  .hasMessageContaining("Converter missing")
                  .hasMessageContaining(String.class + " with value=")
                  .hasMessageContaining(" cannot be converted to " + InputStream.class);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetStringAsBoolean() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": \"true\"}");
        assertThat(map.getAs("a", Boolean.class)).isTrue();
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetStringAsInteger() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": \"1234\"}");
        assertThat(map.getAs("a", Integer.class)).isEqualTo(1234);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetStringAsLong() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": \"1234\"}");
        assertThat(map.getAs("a", Long.class)).isEqualTo(1234L);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetAsDouble() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234.0}");
        assertThat(map.getAs("a", Double.class)).isEqualTo(1234.0);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetLongAsDouble() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1000000000000}");
        assertThat(map.getAs("a", Double.class)).isEqualTo(1000000000000L);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetStringAsDouble() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": \"1234.0\"}");
        assertThat(map.getAs("a", Double.class)).isEqualTo(1234.0);
        assertThat(map.getAs("a", BigDecimal.class)).isEqualTo(new BigDecimal("1234.0"));
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetNullAsDouble() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234.0}");
        assertThat(map.getAs("b", Double.class)).isNull();
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetAsBigDecimal() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234.0}");
        assertThat(map.getAs("a", BigDecimal.class)).isEqualTo(new BigDecimal("1234.0"));
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetStringAsBigDecimal() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": \"1234.0\"}");
        assertThat(map.getAs("a", BigDecimal.class)).isEqualTo(new BigDecimal("1234.0"));
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canGetNullAsBigDecimal() {
        final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234.0}");
        assertThat(map.getAs("b", BigDecimal.class)).isNull();
    }

    @Test
    void booleanMapValueIsBoolean() {
        assertThat(cars.get("name", "audi").get("metallic")).isEqualTo(true);
        assertThat(cars.get(0).is("metallic")).isTrue();
    }

    @Test
    void booleanMapValueMissingIsFalse() {
        assertThat(cars.get(0).is("nicht in der map")).isFalse();
    }

    @Test
    void mapValueContainsKey() {
        assertThat(cars.get(0).mapContainsKey("translation", "1")).isTrue();
    }

    @Test
    void mapValueContainsKeyCondition() {
        assertThat(cars.get(0).mapContainsKey("translation", "1z", false)).isTrue();
        assertThat(cars.get(0).mapContainsKey("not a valid key", "1z", false)).isTrue();
    }

    @Test
    void mapValueDoesntContainKey() {
        assertThat(cars.get(0).mapContainsKey("translation", "4")).isFalse();
    }

    @Test
    void missingMapDoesntContainKey() {
        assertThat(cars.get(0).mapContainsKey("not a valid key", "1")).isFalse();
    }

    @Test
    void mapValueContainsValue() {
        assertThat(cars.get(0).mapContainsValue("translation", "eins")).isTrue();
    }

    @Test
    void mapValueContainsValueCondition() {
        assertThat(cars.get(0).mapContainsValue("translation", "bingo", false)).isTrue();
        assertThat(cars.get(0).mapContainsValue("not a valid key", "bingo", false)).isTrue();
    }

    @Test
    void mapValueDoesntContainValue() {
        assertThat(cars.get(0).mapContainsValue("translation", "vier")).isFalse();
    }

    @Test
    void mapValueCanBeGotten() {
        assertThat((String) cars.get(0).getMapValue("translation", "1")).isEqualTo("eins");
    }

    @Test
    void notExistingMapValueCantBeGotten() {
        assertThat((String) cars.get(0).getMapValue("translation", "4")).isNull();
    }

    @Test
    void mapValueCantBeGottenFromNotExistingMap() {
        assertThat((String) cars.get(0).getMapValue("ralf", "1")).isNull();
    }

    @Test
    void missingMapDoesntContainValue() {
        assertThat(cars.get(0).mapContainsValue("not a valid key", "eins")).isFalse();
    }

    @Test
    void listValueContains() {
        assertThat(cars.get(0).listContains("extras", "tire")).isTrue();
    }

    @Test
    void listValueContainsCondition() {
        assertThat(cars.get(0).listContains("extras", "hustensaft", false)).isTrue();
    }

    @Test
    void listValueDoesntContain() {
        assertThat(cars.get(0).listContains("extras", "mirror")).isFalse();
    }

    @Test
    void missingListValueDoesntContain() {
        assertThat(cars.get(0).listContains("not a valid key", "tire")).isFalse();
    }

    @Test
    void jsonMapCanBeFiltered() {
        final List<JsonMap> filteredCars = cars.stream()
                                               .filter(jm -> jm.listContains("extras", "tire"))
                                               .filter(jm -> jm.mapContainsKey("translation", "1"))
                                               .filter(jm -> jm.mapContainsValue("translation", "nonsense"))
                                               .filter(jm -> jm.is("metallic"))
                                               .filter(jm -> jm.is("color", "pale blue"))
                                               .filter(jm -> jm.getAs("extras", List.class).contains("horn"))
                                               .collect(Collectors.toList());

        assertThat(filteredCars).hasSize(1);
        assertThat(filteredCars.get(0).getAsString("name")).isEqualTo("toyota");
        assertThat(filteredCars.get(0).getAs("price", BigInteger.class)).isEqualTo(new BigInteger("370000000000000000"));
    }

    @Test
    void convertersForEnumsAreImplicitlyRegisteredIfNecessary() {
        enum ABC {A, B, C}

        assertThat(TypeConverter.getRegistrations().contains(ABC.class)).isFalse();
        assertThat(cars.get("name", "mercedes").getAsMap("mapOfMaps").getAsMap("map1").getAs("b", ABC.class)).isEqualTo(ABC.B);
        assertThat(TypeConverter.getRegistrations().contains(ABC.class)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void objectsAreSameAfterOptimization() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/cars.yml");
        assertThat(yamlStream).isNotNull();

        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        assertThat(jsonMap.isOptimizedFor(JsonMapImpl.class)).isTrue();

        final List<JsonMapImpl> carsByCast = (List<JsonMapImpl>) jsonMap.get("cars");
        final JsonList carsByGet = jsonMap.getAsJsonList("cars", JsonMapImpl.class);
        assertThat(carsByGet).isSameAs(carsByCast);

        final JsonMapImpl mercedesByCast = carsByCast.get(2);
        final JsonMap mercedesByGet = carsByGet.get(2);
        assertThat(mercedesByCast.isOptimizedFor(JsonMapImpl.class)).isTrue();
        assertThat(mercedesByGet).isSameAs(mercedesByCast);

        final JsonMapImpl mapOfMapsByCast = (JsonMapImpl) mercedesByCast.get("mapOfMaps");
        final JsonMapImpl mapOfMapsByGet = mercedesByGet.getAsMap("mapOfMaps", JsonMapImpl.class);
        assertThat(mapOfMapsByCast.isOptimizedFor(JsonMapImpl.class)).isTrue();
        assertThat(mapOfMapsByGet).isSameAs(mapOfMapsByCast);

        final JsonMapImpl map1ByCast = (JsonMapImpl) mapOfMapsByCast.get("map1");
        final JsonMapImpl map1ByGet = mapOfMapsByGet.getAsMap("map1", JsonMapImpl.class);
        assertThat(map1ByCast.isOptimizedFor(JsonMapImpl.class)).isTrue();
        assertThat(map1ByGet).isSameAs(map1ByCast);

        final JsonMapImpl map2ByCast = (JsonMapImpl) mapOfMapsByCast.get("map2");
        final JsonMapImpl map2ByGet = mapOfMapsByGet.getAsMap("map2", JsonMapImpl.class);
        assertThat(map2ByCast.isOptimizedFor(JsonMapImpl.class)).isTrue();
        assertThat(map2ByGet).isSameAs(map2ByCast);
    }

    @Test
    void mapCannotBeOptimizedForInterface() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/cars.yml");
        assertThat(yamlStream).isNotNull();
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        assertThatThrownBy(() -> jsonMap.optimize(JsonMap.class)).isInstanceOf(IllegalArgumentException.class);
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