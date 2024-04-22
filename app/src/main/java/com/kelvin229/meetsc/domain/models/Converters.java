package com.kelvin229.meetsc.domain.models;

import androidx.room.TypeConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Converters {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    @TypeConverter
    public static Duration fromDurationLong(Long value) {
        return value == null ? null : Duration.ofMinutes(value);
    }

    @TypeConverter
    public static Long durationToLong(Duration duration) {
        return duration == null ? null : duration.toMinutes();
    }

    @TypeConverter
    public static LocalDateTime fromTimestamp(String value) {
        return value == null ? null : LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    @TypeConverter
    public static String dateToTimestamp(LocalDateTime date) {
        return date == null ? null : date.format(DATE_TIME_FORMATTER);
    }

    @TypeConverter
    public static String fromSetToString(Set<String> value) {
        return value == null ? null : String.join(",", value);
    }

    @TypeConverter
    public static Set<String> fromStringToSet(String value) {
        return value == null || value.isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(value.split(",")));
    }
}