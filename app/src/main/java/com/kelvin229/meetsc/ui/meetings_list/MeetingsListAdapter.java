package com.kelvin229.meetsc.ui.meetings_list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kelvin229.meetsc.databinding.MeetingRecyclerviewItemBinding;
import com.kelvin229.meetsc.domain.models.MeetingWithLocation;
import com.kelvin229.meetsc.events.DeleteMeetingEvent;
import com.kelvin229.meetsc.events.EditMeetingEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MeetingsListAdapter extends RecyclerView.Adapter<MeetingsListAdapter.ViewHolder> {

    private List<MeetingWithLocation> meetingsWithLocations;

    public MeetingsListAdapter(List<MeetingWithLocation> meetingsWithLocations) {
        this.meetingsWithLocations = meetingsWithLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(MeetingRecyclerviewItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeetingWithLocation current = meetingsWithLocations.get(position);
        String locationName = current.locationName != null ? current.locationName : "Unknown Location";
        String titleWithLocation = current.meeting.getTitle() + " (" + locationName + ")";
        holder.itemBinding.meetingTitle.setText(titleWithLocation);
        holder.itemBinding.meetingSubtitle.setText(current.meeting.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return meetingsWithLocations != null ? meetingsWithLocations.size() : 0;
    }

    public void updateMeetings(List<MeetingWithLocation> updatedMeetings) {
        this.meetingsWithLocations = updatedMeetings;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MeetingRecyclerviewItemBinding itemBinding;

        public ViewHolder(@NonNull MeetingRecyclerviewItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
            itemBinding.editMeeting.setOnClickListener(view -> editMeeting(getAdapterPosition()));
            itemBinding.deleteMeeting.setOnClickListener(view -> emitMeetingDeletionEvent(meetingsWithLocations.get(getAdapterPosition())));
        }

        private void editMeeting(int position) {
            MeetingWithLocation meetingToEdit = meetingsWithLocations.get(position);
            EventBus.getDefault().post(new EditMeetingEvent(meetingToEdit.meeting.getId()));
        }

        private void emitMeetingDeletionEvent(MeetingWithLocation meetingWithLocation) {
            EventBus.getDefault().post(new DeleteMeetingEvent(meetingWithLocation.meeting));
        }
    }
}

