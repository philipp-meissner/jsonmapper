// Copyright (c) by Philipp Meißner 2022.

package de.phil.json.typeconverter;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Map for Type Converters.
 */
class TypeConverterMap extends HashMap<Class<?>, Function<Object, ?>> {
    @Override
    public Function<Object, ?> put(Class<?> key, Function<Object, ?> value) {
        return super.put(key, converterWrapper(key, value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Function<Object, ?> get(Object key) {
        if (!containsKey(key) && (key instanceof Class<?>) && ((Class<?>) key).isEnum()) {
            put((Class<?>) key, converter((Class<Enum<?>>) key));
        }
        return super.get(key);
    }

    @NotNull
    private Function<Object, Object> converterWrapper(Class<?> key, Function<Object, ?> value) {
        return o -> {
            try {
                return value.apply(o);
            } catch (TypeConversionException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new TypeConversionException(o, key, e);
            }
        };
    }

    @SuppressWarnings("RedundantCast")
    @SneakyThrows
    @NotNull
    private static Function<Object, Object> converter(Class<Enum<?>> enumClass) {
        final Method valueOf = enumClass.getDeclaredMethod("valueOf", String.class);
        valueOf.setAccessible(true);
        return o -> {
            try {
                return valueOf.invoke(enumClass, (String) o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
