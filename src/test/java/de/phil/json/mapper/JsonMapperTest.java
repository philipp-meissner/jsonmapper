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
 * Test für die Klasse {@link JsonMapper}.
 */
class JsonMapperTest {

    private static final String NAME = "Hans Dietrich Genscher";
    private static final Instant MEMBER_SINCE = Instant.now();

    @Test
    void canWriteAndReadString() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final String json = JsonMapper.writeValueAsString(person);
        assertThat(JsonMapper.readJson(json, Person.class)).isEqualTo(person);
    }

    @Test
    void canCopy() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final JsonMap map = JsonMapper.writeValueAsMap(person);
        assertThat(JsonMapper.copyValue(map, Person.class)).isEqualTo(person);
    }

    @Test
    void canWriteToSpecificMap() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final MemberJsonMap map = JsonMapper.writeValueAsMap(person, MemberJsonMap.class);
        assertThat(Instant.parse(map.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
        assertThat(map.getMemberSince()).isEqualTo(MEMBER_SINCE);
    }

    @Test
    void canReadFromSpecificMap() {
        final Person person = new Person(NAME, MEMBER_SINCE);
        final String json = JsonMapper.writeValueAsString(person);
        final MemberJsonMap map = JsonMapper.readJson(json, MemberJsonMap.class);
        assertThat(Instant.parse(map.getAsString(Person.Fields.memberSince))).isEqualTo(MEMBER_SINCE);
        assertThat(map.getMemberSince()).isEqualTo(MEMBER_SINCE);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void canReadYaml() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        final JsonList persons = jsonMap.getAsJsonList("persons");
        assertThat(persons.get(0).getAsString("lastName")).isEqualTo("Genscher");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void canWriteYaml() throws IOException {
        final InputStream yamlStream = getClass().getResourceAsStream("/person.yml");
        final String yaml = IOUtils.toString(yamlStream, StandardCharsets.UTF_8);
        final JsonMapImpl jsonMap = JsonMapper.readYaml(yaml, JsonMapImpl.class);
        final String asYaml = JsonMapper.writeValueAsYaml(jsonMap);
        Assertions.assertThat(asYaml).isEqualTo(yaml);
    }

}
