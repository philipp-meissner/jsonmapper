// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Exception zur Anzeige, dass ein Wert nicht konvertiert werden kann.
 */
public class TypeConversionException extends RuntimeException {
    @Contract(pure = true)
    @NotNull
    private static String getMessage(Object valueToConvert, Class<?> classToConvertTo) {
        return valueToConvert.getClass() + " with value=" + valueToConvert + " cannot be converted to " + classToConvertTo;
    }

    public TypeConversionException(Object valueToConvert, Class<?> classToConvertTo) {
        super(getMessage(valueToConvert, classToConvertTo));
    }

    public TypeConversionException(String prefix, Object valueToConvert, Class<?> classToConvertTo, String postfix) {
        super(prefix + getMessage(valueToConvert, classToConvertTo) + postfix);
    }

    public TypeConversionException(Object valueToConvert, Class<?> classToConvertTo, Throwable cause) {
        super(getMessage(valueToConvert, classToConvertTo), cause);
    }
}
