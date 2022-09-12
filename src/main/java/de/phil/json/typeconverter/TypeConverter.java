// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Konvertiert Typen.
 */
public class TypeConverter {
    private static final TypeConverterMap CONVERTERS = new TypeConverterMap();
    private static final TypeConverterMap DEFAULT_CONVERTERS = new TypeConverterMap();

    static {
        // NB: Für enums muss i.d.R. nicht explizit ein Konverter registriert werden.
        // Die ConverterMap erstellt bei Bedarf generische Konverter für enums

        // Standard-Konverter
        registerDefault(String.class, String::valueOf);
        registerDefault(Boolean.class, value -> {
            if (value instanceof String s) {
                return Boolean.valueOf(s);
            }
            throw new TypeConversionException(value, Boolean.class);
        });
        registerDefault(Integer.class, value -> {
            if (value instanceof String s) {
                return Integer.valueOf(s);
            }
            throw new TypeConversionException(value, Integer.class);
        });
        registerDefault(Long.class, value -> {
            if (value instanceof Integer i) {
                return Long.valueOf(i);
            }
            if (value instanceof String s) {
                return Long.valueOf(s);
            }
            throw new TypeConversionException(value, Long.class);
        });
        registerDefault(Double.class, value -> {
            if (value instanceof Integer i) {
                return Double.valueOf(i);
            }
            if (value instanceof Long l) {
                return Double.valueOf(l);
            }
            if (value instanceof String s) {
                return Double.valueOf(s);
            }
            throw new TypeConversionException(value, Double.class);
        });
        registerDefault(BigInteger.class, value -> {
            if (value instanceof Integer i) {
                return BigInteger.valueOf(i);
            }
            if (value instanceof Long l) {
                return BigInteger.valueOf(l);
            }
            if (value instanceof String s) {
                return new BigInteger(s);
            }
            throw new TypeConversionException(value, BigInteger.class);
        });
        registerDefault(BigDecimal.class, value -> {
            if (value instanceof Integer i) {
                return BigDecimal.valueOf(i);
            }
            if (value instanceof Long l) {
                return BigDecimal.valueOf(l);
            }
            if (value instanceof Double d) {
                return BigDecimal.valueOf(d);
            }
            if (value instanceof String s) {
                return new BigDecimal(s);
            }
            throw new TypeConversionException(value, BigDecimal.class);
        });

        // Datums-Konverter
        registerDefault(LocalDate.class, value -> LocalDate.parse((String) value));
        registerDefault(LocalTime.class, value -> LocalTime.parse((String) value));
        registerDefault(OffsetTime.class, value -> OffsetTime.parse((String) value));
        registerDefault(OffsetDateTime.class, value -> OffsetDateTime.parse((String) value));
        registerDefault(ZonedDateTime.class, value -> ZonedDateTime.parse((String) value));
        registerDefault(Instant.class, value -> Instant.parse((String) value));
    }

    /**
     * Registriert einen Konverter.
     *
     * @param transformer Konverter.
     */
    private static <T> void registerDefault(Class<T> clazz, Function<Object, T> transformer) {
        DEFAULT_CONVERTERS.put(clazz, transformer);
        CONVERTERS.put(clazz, transformer);
    }

    /**
     * Registriert einen Konverter.
     *
     * @param transformer Konverter.
     */
    public static <T> void register(Class<T> clazz, Function<Object, T> transformer) {
        CONVERTERS.put(clazz, transformer);
    }

    /**
     * Deregistriert einen Konverter.
     *
     * @param clazz Klasse des Konverters.
     */
    public static void unregister(Class<?> clazz) {
        CONVERTERS.remove(clazz);
    }

    /**
     * Registriert einen Konverter, wenn dieser noch nicht registriert ist.
     * @param key   Klasse, in die konvertiert werden kann.
     * @param mappingFunction Funktion zum Konvertieren.
     */
    public static void registerIfAbsent(Class<?> key, Function<Object, ?> mappingFunction) {
        CONVERTERS.computeIfAbsent(key, x -> mappingFunction);
    }

    /**
     * Liefert die Klassen, für die Default-Konverter registriert sind.
     * @return Klassen, für die Default-Konverter registriert sind.
     */
    static Set<Class<?>> getDefaultRegistrations() {
        return DEFAULT_CONVERTERS.keySet();
    }

    /**
     * Liefert die Klassen, für die Konverter registriert sind.
     * @return Klassen, für die Konverter registriert sind.
     */
    public static Set<Class<?>> getRegistrations() {
        return CONVERTERS.keySet();
    }

    /**
     * Prüft, ob in die gegebene Klasse konvertiert werden kann.
     * @param clazz Klasse, in die konvertiert werden soll.
     * @return Kann in die Klasse konvertiert werden?
     */
    public static boolean canConvertTo(Class<?> clazz) {
        return CONVERTERS.containsKey(clazz) || ((clazz != null) && clazz.isEnum());
    }

    /**
     * Konvertiert den gegebenen Wert in die gegebene Klasse.
     * @param value     Zu konvertierender Wert.
     * @param toClass   Klasse des gewünschten Types.
     * @param <T>       Gewünschter Typ.
     * @return Konvertierter Wert.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> toClass) {
        return switch (TypeConversionInfo.of(value, toClass)) {
            case NULL -> null;
            case CAST -> (T) value;
            case CONVERT -> (T) CONVERTERS.get(toClass).apply(value);
            default -> throw new TypeConversionException("Converter missing: ", value, toClass,
                                                         ". Converters are registered for " + TypeConverter.getRegistrations()
                                                                                                           .stream()
                                                                                                           .map(Class::getSimpleName)
                                                                                                           .sorted()
                                                                                                           .collect(Collectors.joining(", ")));
        };
    }

}
