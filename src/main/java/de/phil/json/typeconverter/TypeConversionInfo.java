// Copyright (c) by go4medical.eu 2022.

package de.phil.json.typeconverter;

/**
 * Informationen, ob und wie ein Wert konvertiert werden kann.
 */
enum TypeConversionInfo {
    NULL,
    CAST,
    CONVERT,
    IMPOSSIBLE;

    /**
     * Liefert Informationen, ob und wie ein Wert konvertiert werden kann.
     *
     * @param value Zu konvertierender Wert.
     * @param clazz Klasse, in die der Wert konvertiert werden soll.
     * @param <T>   Typ der Klasse, in die der Wert konvertiert werden soll.
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
