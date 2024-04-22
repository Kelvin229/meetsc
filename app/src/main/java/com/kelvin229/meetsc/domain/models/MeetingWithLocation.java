package com.kelvin229.meetsc.domain.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class MeetingWithLocation {

    @Embedded
    public Meeting meeting;
    @ColumnInfo(name = "locationName")
    public String locationName;

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}