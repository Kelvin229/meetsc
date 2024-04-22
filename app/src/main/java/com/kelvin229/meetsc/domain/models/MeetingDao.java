package com.kelvin229.meetsc.domain.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MeetingDao {

    @Insert
    long insertMeeting(Meeting meeting);

    @Update
    void updateMeeting(Meeting meeting);

    @Delete
    void deleteMeeting(Meeting meeting);

    @Query("SELECT * FROM meetings")
    List<Meeting> getAllMeetings();

    @Query("SELECT * FROM meetings WHERE id = :meetingId")
    Meeting getMeetingById(int meetingId);

    @Query("DELETE FROM meetings")
    void deleteAllMeetings();

    @Query("DELETE FROM meetings WHERE id = :meetingId")
    void deleteMeetingById(int meetingId);

    @Query("DELETE FROM meetings WHERE location_id = :locationId")
    void deleteMeetingsByLocationId(int locationId);

    @Query("DELETE FROM meetings WHERE date(start_time) = :date")
    void deleteMeetingsByDate(String date);

    @Query("SELECT Meetings.*, Locations.name AS locationName FROM Meetings JOIN Locations ON Meetings.location_id = Locations.id")
    List<MeetingWithLocation> getMeetingsWithLocationNames();

    @Query("SELECT Meetings.*, Locations.name AS locationName FROM Meetings JOIN Locations ON Meetings.location_id = Locations.id WHERE date(Meetings.start_time) = :date")
    List<MeetingWithLocation> getMeetingsWithLocationNamesByDate(String date);

    @Query("SELECT Meetings.*, Locations.name AS locationName FROM Meetings JOIN Locations ON Meetings.location_id = Locations.id WHERE Meetings.location_id = :locationId")
    List<MeetingWithLocation> getMeetingsWithLocationNamesByLocationId(int locationId);

}

