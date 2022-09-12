package de.phil.json.typeconverter;

import de.phil.json.mapper.impl.JsonMapImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test für die Klasse {@link TypeConverter}.
 */
class TypeConverterTest {

    private static Stream<Arguments> conversions() {
        enum ABC{A, B, C}
        return Stream.of(
                Arguments.of(1234, String.class, "1234"),
                Arguments.of(1234L, String.class, "1234"),
                Arguments.of("true", Boolean.class, true),
                Arguments.of("false", Boolean.class, false),
                Arguments.of("1234", Integer.class, 1234),
                Arguments.of(1234, Long.class, 1234L),
                Arguments.of("1234", Long.class, 1234L),
                Arguments.of(1234, Double.class, 1234.0),
                Arguments.of(1234L, Double.class, 1234.0),
                Arguments.of("1234", Double.class, 1234.0),
                Arguments.of(1234, BigInteger.class, BigInteger.valueOf(1234)),
                Arguments.of(1234L, BigInteger.class, BigInteger.valueOf(1234)),
                Arguments.of("1234", BigInteger.class, BigInteger.valueOf(1234)),
                Arguments.of(1234, BigDecimal.class, BigDecimal.valueOf(1234)),
                Arguments.of(1234L, BigDecimal.class, BigDecimal.valueOf(1234)),
                Arguments.of(1234.0, BigDecimal.class, BigDecimal.valueOf(1234.0)),
                Arguments.of("1234.0", BigDecimal.class, BigDecimal.valueOf(1234.0)),
                Arguments.of("2022-05-07", LocalDate.class, LocalDate.parse("2022-05-07")),
                Arguments.of("15:37:56", LocalTime.class, LocalTime.parse("15:37:56")),
                Arguments.of("15:37:56+01:00", OffsetTime.class, OffsetTime.parse("15:37:56+01:00")),
                Arguments.of("2022-05-07T15:37:56+01:00", OffsetDateTime.class, OffsetDateTime.parse("2022-05-07T15:37:56+01:00")),
                Arguments.of("2022-05-07T10:15:30+01:00[Europe/Paris]", ZonedDateTime.class, ZonedDateTime.parse("2022-05-07T10:15:30+01:00[Europe/Paris]")),
                Arguments.of("2022-05-07T10:15:30Z", Instant.class, Instant.parse("2022-05-07T10:15:30Z")),
                Arguments.of("A", ABC.class, ABC.A),
                Arguments.of(null, Integer.class, null),
                Arguments.of(1234, Integer.class, 1234)
                        );
    }

    @ParameterizedTest
    @MethodSource("conversions")
    void canConvertTo(Object value, Class<?> clazz, Object expected) {
        assertThat(TypeConverter.canConvertTo(clazz)).isTrue();
        assertThat(TypeConverter.convert(value, clazz)).isEqualTo(expected);
    }

    private static Stream<Class<?>> converterClasses() {
        return TypeConverter.getDefaultRegistrations()
                            .stream()
                            .filter(c -> c != String.class);
    }

    @ParameterizedTest
    @MethodSource("converterClasses")
    void throwsExceptionOnIllegalConversion(Class<?> clazz) {
        assertThatThrownBy(() -> TypeConverter.convert(this, clazz))
                .isInstanceOf(TypeConversionException.class)
                .hasMessageContaining(getClass() + " with value=")
                .hasMessageContaining(" cannot be converted to " + clazz);
    }



    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void canUnregisterConverter() {
        try {
            final JsonMapImpl map = new JsonMapImpl("{\"a\": 1234}");
            final Date d = new Date();
            TypeConverter.register(Date.class, value -> d);
            assertThat(map.getAs("a", Date.class)).isEqualTo(d);
            TypeConverter.unregister(Date.class);
            assertThatThrownBy(() -> map.getAs("a", Date.class))
                      .isInstanceOf(TypeConversionException.class)
                      .hasMessageContaining("value=1234 cannot be converted to " + Date.class);
        } finally {
            TypeConverter.unregister(Date.class);
        }
    }


}