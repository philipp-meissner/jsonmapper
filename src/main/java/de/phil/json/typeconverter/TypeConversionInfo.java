// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

/**
 * Informationen about if and how a value can be converted.
 */
enum TypeConversionInfo {
    NULL,
    CAST,
    CONVERT,
    IMPOSSIBLE;

    /**
     * Gets informationen about if and how a value can be converted.
     *
     * @param value  Value to convert.
     * @param clazz  Class to convert to.
     * @return Informationen zur Konvertierung.
     */
    public static <T> TypeConversionInfo of(Object value, Class<T> clazz) {
        if (value == null) {
            return NULL;
        }
        if (clazz.isAssignableFrom(value.getClass())) {
            return CAST;
        }
        return TypeConverter.canConvertTo(clazz)
                ? CONVERT
                : IMPOSSIBLE;
    }
}
