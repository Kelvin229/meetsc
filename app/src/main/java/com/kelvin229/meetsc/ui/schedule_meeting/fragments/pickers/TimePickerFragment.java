package com.kelvin229.meetsc.ui.schedule_meeting.fragments.pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.time.LocalTime;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {
    private final TimePickerDialog.OnTimeSetListener listener;
    private final LocalTime initialTime;

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener, LocalTime initialTime) {
        this.listener = listener;
        this.initialTime = initialTime;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = initialTime != null ? initialTime.getHour() : c.get(Calendar.HOUR_OF_DAY);
        int minute = initialTime != null ? initialTime.getMinute() : c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), listener, hour, minute, true);
    }
}
