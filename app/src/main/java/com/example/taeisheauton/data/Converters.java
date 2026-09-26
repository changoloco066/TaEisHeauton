package com.example.taeisheauton.data;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static String fromSourceType(SourceType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static SourceType toSourceType(String value) {
        return value == null ? null : SourceType.valueOf(value);
    }
}
