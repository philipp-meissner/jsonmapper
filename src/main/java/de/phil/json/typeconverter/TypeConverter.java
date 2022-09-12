// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts types.
 */
public class TypeConverter {
    private static final TypeConverterMap CONVERTERS = new TypeConverterMap();
    private static final TypeConverterMap DEFAULT_CONVERTERS = new TypeConverterMap();

    static {
        // NB: usually no explicit registration for enums needed as they are automatically created and registered if needed

        // standard converters
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

        // date converters
        registerDefault(LocalDate.class, value -> LocalDate.parse((String) value));
        registerDefault(LocalTime.class, value -> LocalTime.parse((String) value));
        registerDefault(OffsetTime.class, value -> OffsetTime.parse((String) value));
        registerDefault(OffsetDateTime.class, value -> OffsetDateTime.parse((String) value));
        registerDefault(ZonedDateTime.class, value -> ZonedDateTime.parse((String) value));
        registerDefault(Instant.class, value -> Instant.parse((String) value));
    }

    /**
     * Registers a default converter.
     *
     * @param converter Converter.
     */
    private static <T> void registerDefault(Class<T> clazz, Function<Object, T> converter) {
        DEFAULT_CONVERTERS.put(clazz, converter);
        CONVERTERS.put(clazz, converter);
    }

    /**
     * Registers a converter.
     * @param clazz  Class to convert to.
     * @param converter  Converter.
     * @param <T> Typ des Konverters.
     */
    public static <T> void register(Class<T> clazz, Function<Object, T> converter) {
        CONVERTERS.put(clazz, converter);
    }

    /**
     * Unregisters a converter.
     * @param clazz  Converter class.
     */
    public static void unregister(Class<?> clazz) {
        CONVERTERS.remove(clazz);
    }

    /**
     * Registers a converter if absent.
     * @param clazz  Class to convert to.
     * @param converter  Converter.
     */
    public static void registerIfAbsent(Class<?> clazz, Function<Object, ?> converter) {
        CONVERTERS.computeIfAbsent(clazz, x -> converter);
    }

    /**
     * Gets registrations of default converters.
     * @return classes of default converters.
     */
    static Set<Class<?>> getDefaultRegistrations() {
        return DEFAULT_CONVERTERS.keySet();
    }

    /**
     * Gets registrations of converters.
     * @return classes of converters.
     */
    public static Set<Class<?>> getRegistrations() {
        return CONVERTERS.keySet();
    }

    /**
     * Checks whether conversion is possible.
     * @param clazz  Class to convert to.
     * @return true if conversion is possible.
     */
    public static boolean canConvertTo(Class<?> clazz) {
        return CONVERTERS.containsKey(clazz) || ((clazz != null) && clazz.isEnum());
    }

    /**
     * Converts a value into the given class.
     * @param value    Value to convert.
     * @param toClass  Class to convert to.
     * @param <T>      Type of converted class.
     * @return Converted value.
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
