package com.kelvin229.meetsc.domain.models;

import androidx.room.ColumnInfo;
import java.time.Duration;
import java.time.LocalDateTime;
public class TimeSlot {

    @ColumnInfo(name = "start_time")
    private LocalDateTime startTime;

    @ColumnInfo(name = "duration")
    private Duration duration;

    public TimeSlot(LocalDateTime startTime, Duration duration) {
        this.startTime = startTime;
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public boolean overlapsWith(TimeSlot otherSlot) {
        return !startTime.isAfter(otherSlot.getEndTime()) && !otherSlot.getStartTime().isAfter(getEndTime());
    }
}

