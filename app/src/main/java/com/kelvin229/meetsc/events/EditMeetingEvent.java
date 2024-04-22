package com.kelvin229.meetsc.events;

public class EditMeetingEvent {
    private final int meetingId;

    public EditMeetingEvent(int meetingId) {
        this.meetingId = meetingId;
    }

    public int getMeetingId() {
        return meetingId;
    }
}
