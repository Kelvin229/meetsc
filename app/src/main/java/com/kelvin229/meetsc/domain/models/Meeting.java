package com.kelvin229.meetsc.domain.models;

import static com.kelvin229.meetsc.domain.models.Converters.fromSetToString;
import static com.kelvin229.meetsc.domain.models.Converters.fromStringToSet;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(tableName = "meetings")
public class Meeting implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "participant_emails")
    private String participantEmails;

    @Embedded
    private TimeSlot timeSlot;

    @ColumnInfo(name = "location_id")
    private int locationId;

    @ColumnInfo(name = "subject")
    private String subject;

    public Meeting() {}

    public Meeting(Set<String> participantEmails, TimeSlot timeSlot, int locationId, String subject) {
        this.participantEmails = fromSetToString(participantEmails);
        this.timeSlot = timeSlot;
        this.locationId = locationId;
        this.subject = subject;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParticipantEmails() {
        return participantEmails;
    }

    public void setParticipantEmails(String participantEmails) {
        this.participantEmails = participantEmails;
    }

    public Set<String> getParticipantEmailsSet() {
        return fromStringToSet(this.participantEmails);
    }

    public void setParticipantEmailsSet(Set<String> participantEmails) {
        this.participantEmails = fromSetToString(participantEmails);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getDateTime() {
        return this.timeSlot.getStartTime();
    }

    public Duration getDuration() {
        return this.timeSlot.getDuration();
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getTitle() {
        return joinStrings( getFormattedTime(), subject);
    }

    public String getSubtitle() {
        return joinStrings(", ", getParticipantEmailsSet().toArray(new String[0]));
    }

    public String getFormattedTime() {
        return getDateTime().format(DateTimeFormatter.ofPattern("HH'h'mm"));
    }

    @NonNull
    private String joinStrings(String delimiter, String... strings) {
        final List<String> stringsList = Arrays.asList(strings);
        return String.join(delimiter, stringsList);
    }

    @NonNull
    @Override
    public String toString() {
        return "Meeting on " + getDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " at " + getLocationId() + ": " + getSubject();
    }
}