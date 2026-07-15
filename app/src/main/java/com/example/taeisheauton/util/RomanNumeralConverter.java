package com.example.taeisheauton.util;

import java.util.Map;

public class RomanNumeralConverter {

    private static final Map<String, Integer> ROMAN_VALUES = Map.ofEntries(
            Map.entry("I", 1),
            Map.entry("II", 2),
            Map.entry("III", 3),
            Map.entry("IV", 4),
            Map.entry("V", 5),
            Map.entry("VI", 6),
            Map.entry("VII", 7),
            Map.entry("VIII", 8),
            Map.entry("IX", 9),
            Map.entry("X", 10),
            Map.entry("XI", 11),
            Map.entry("XII", 12)
    );

    public static int toInt(String roman) {
        String normalized = roman.toUpperCase();
        return ROMAN_VALUES.get(normalized);
    }
}
