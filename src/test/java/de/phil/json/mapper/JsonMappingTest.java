package de.phil.json.mapper;

import de.phil.json.mapper.impl.JsonMapImpl;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JsonMapping}.
 */
class JsonMappingTest {

    private static final String NAME = "Hans Dietrich Genscher";
    private static final Instant MEMBER_SINCE = Instant.now();

    @Test
    void canWriteAndReadString() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final String json = JsonMapping.writeValueAsString(person);
        assertThat(JsonMapping.readJson(json, Person.class)).isEqualTo(person);
    }

    @Test
    void canCopy() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final JsonMap map = JsonMapping.writeValueAsMap(person);
        assertThat(JsonMapping.copyValue(map, Person.class)).isEqualTo(person);
    }

    @Test
    void canWriteToSpecificMap() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final MemberJsonMap map = JsonMapping.writeValueAsMap(person, MemberJsonMap.class);
        assertThat(Instant.parse(map.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
        assertThat(map.getMemberSince()).isEqualTo(MEMBER_SINCE);
    }

    @Test
    void canReadFromSpecificMap() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final String json = JsonMapping.writeValueAsString(person);
        final MemberJsonMap map = JsonMapping.readJson(json, MemberJsonMap.class);
        assertThat(Instant.parse(map.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
        assertThat(map.getMemberSince()).isEqualTo(MEMBER_SINCE);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void canReadYaml() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapping.readYaml(yaml, JsonMapImpl.class);
        final JsonList persons = jsonMap.getAsJsonList("persons");
        assertThat(persons.get(0).getAsString("lastName")).isEqualTo("Genscher");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void canWriteYaml() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapping.readYaml(yaml, JsonMapImpl.class);
        final String asYaml = JsonMapping.writeValueAsYaml(jsonMap);
        Assertions.assertThat(asYaml).isEqualTo(yaml);
    }

}
