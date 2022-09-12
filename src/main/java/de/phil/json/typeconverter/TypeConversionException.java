// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Type conversion failed.
 */
public class TypeConversionException extends RuntimeException {
    @Contract(pure = true)
    @NotNull
    private static String getMessage(Object valueToConvert, Class<?> classToConvertTo) {
        return valueToConvert.getClass() + " with value=" + valueToConvert + " cannot be converted to " + classToConvertTo;
    }

    /**
     * Type conversion failed.
     * @param valueToConvert    Value to convert.
     * @param classToConvertTo  Class to convert to.
     */
    public TypeConversionException(Object valueToConvert, Class<?> classToConvertTo) {
        super(getMessage(valueToConvert, classToConvertTo));
    }

    /**
     * Type conversion failed.
     * @param prefix            Prefix for error message.
     * @param valueToConvert    Value to convert.
     * @param classToConvertTo  Class to convert to.
     * @param postfix           Postfix for error message.
     */
    public TypeConversionException(String prefix, Object valueToConvert, Class<?> classToConvertTo, String postfix) {
        super(prefix + getMessage(valueToConvert, classToConvertTo) + postfix);
    }

    /**
     * Type conversion failed.
     * @param valueToConvert    Value to convert.
     * @param classToConvertTo  Class to convert to.
     * @param cause             Cause.
     */
    public TypeConversionException(Object valueToConvert, Class<?> classToConvertTo, Throwable cause) {
        super(getMessage(valueToConvert, classToConvertTo), cause);
    }
}
