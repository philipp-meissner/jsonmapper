package de.phil.json.mapper;

import de.phil.json.mapper.impl.JsonMapImpl;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JsonList}.
 */
class JsonListTest {
    private final JsonList cars = givenJsonMapWithCars();

    @Test
    void canGetByProperty() {
        assertThat(cars.get("name", "mercedes").getAsString("name")).isEqualTo("mercedes");
    }

    @Test
    void throwsExceptionForGetByNonUniqueProperty() {
        assertThatThrownBy(() -> cars.get("color", "pale blue"))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("key=color not unique");
    }

    @Test
    void throwsExceptionForGetByNonExistingProperty() {
        assertThatThrownBy(() -> cars.get("bingo", "bongo"))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("key=bingo cannot be found");
    }

    @Test
    void canGetByBooleanFalseProperty() {
        assertThat(cars.get("metallic", false).getAsString("name")).isEqualTo("porsche");
    }

    @Test
    void canGetByBooleanTrueProperty() {
        assertThat(cars.get("4wd", true).getAsString("name")).isEqualTo("porsche");
    }

    @Test
    void throwsExceptionForGetByNonUniqueBooleanProperty() {
        assertThatThrownBy(() -> cars.get("metallic", true))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("key=metallic not unique");
    }

    @Test
    void throwsExceptionForGetByNonExistingBooleanProperty() {
        assertThatThrownBy(() -> cars.get("bingo", true))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("key=bingo cannot be found");
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