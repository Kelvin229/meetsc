package com.kelvin229.meetsc.domain.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface LocationDao {

    @Insert
    long insertLocation(Location location);

    @Query("SELECT COUNT(*) FROM locations WHERE name = :locationName")
    int countLocationsByName(String locationName);

    @Query("SELECT * FROM locations")
    List<Location> getAllLocations();

    @Delete
    void deleteLocation(Location location);

    @Query("DELETE FROM locations")
    void deleteAllLocations();

    @Query("SELECT * FROM locations WHERE name = :name")
    Location getLocationByName(String name);

    @Query("SELECT * FROM locations WHERE id = :id")
    Location getLocationById(int id);

    @Query("SELECT * FROM meetings WHERE location_id = :locationId AND ((start_time BETWEEN :start AND :end) OR (start_time + duration BETWEEN :start AND :end))")
    List<Meeting> getMeetingsAtLocationInTimeSlot(int locationId, LocalDateTime start, LocalDateTime end);
}