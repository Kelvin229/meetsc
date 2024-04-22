package com.kelvin229.meetsc.ui.schedule_meeting.fragments.pickers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kelvin229.meetsc.R;
import com.kelvin229.meetsc.databinding.DurationPickerBinding;

import java.time.Duration;

public class DurationPickerFragment extends DialogFragment {
    private final OnDurationSetListener listener;
    public interface OnDurationSetListener {
        void onDurationSet(Duration duration);
    }

    public DurationPickerFragment(OnDurationSetListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        final DurationPickerBinding viewBinding = DurationPickerBinding.inflate(getActivity().getLayoutInflater());

        viewBinding.hoursPicker.setMaxValue(23);
        viewBinding.minutesPicker.setMaxValue(59);

        dialogBuilder.setView(viewBinding.getRoot())
                .setPositiveButton(getString(R.string.duration_picker_positive_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int hours = viewBinding.hoursPicker.getValue();
                        final int minutes = viewBinding.minutesPicker.getValue() + (hours * 60);
                        listener.onDurationSet(Duration.ofMinutes(minutes));
                    }
                }).setNegativeButton(getString(R.string.duration_picker_negative_btn), null);
        return dialogBuilder.create();
    }
}
