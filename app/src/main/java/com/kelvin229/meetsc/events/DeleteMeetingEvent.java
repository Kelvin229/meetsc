package com.kelvin229.meetsc.events;

import com.kelvin229.meetsc.domain.models.Meeting;

public class DeleteMeetingEvent {
    public final Meeting meetingToDelete;

    public DeleteMeetingEvent(Meeting meetingToDelete){
        this.meetingToDelete = meetingToDelete;
    }
}
