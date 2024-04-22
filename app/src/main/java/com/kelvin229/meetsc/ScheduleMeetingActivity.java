package com.kelvin229.meetsc;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.kelvin229.meetsc.databinding.ActivityScheduleMeetingBinding;
import com.kelvin229.meetsc.domain.models.AppDatabase;
import com.kelvin229.meetsc.domain.models.Meeting;
import com.kelvin229.meetsc.domain.models.MeetingDao;
import com.kelvin229.meetsc.domain.models.TimeSlot;
import com.kelvin229.meetsc.ui.fragments.pickers.DatePickerFragment;
import com.kelvin229.meetsc.ui.schedule_meeting.fragments.pickers.DurationPickerFragment;
import com.kelvin229.meetsc.ui.schedule_meeting.fragments.pickers.TimePickerFragment;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kelvin229.meetsc.domain.models.Location;
import com.kelvin229.meetsc.domain.models.LocationDao;

public class ScheduleMeetingActivity extends AppCompatActivity {

  private ActivityScheduleMeetingBinding viewBinding;
  private LocalDate meetingDate;
  private LocalTime meetingStartTime;
  private Duration meetingDuration;
  private Location selectedLocation;
  private DatePickerFragment datePickerFragment;
  private TimePickerFragment timePickerFragment;
  private DurationPickerFragment durationPickerFragment;
  private LocationDao locationDao;
  private MeetingDao meetingDao;
  private Integer editingMeetingId;
  private final Set<String> participantEmails = new HashSet<>();
  private boolean editMode = false;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ActivityScheduleMeetingBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());

    EditText locationNameEditText = findViewById(R.id.locationNameEditText);

    locationDao = AppDatabase.getDatabase(this).locationDao();
    meetingDao = AppDatabase.getDatabase(this).meetingDao();

    editMode = getIntent().getBooleanExtra("editMode", false);
    if (editMode) {
      editingMeetingId = getIntent().getIntExtra("meetingId", -1);
      if (editingMeetingId != -1) {
        loadMeetingDetails(editingMeetingId);
      }
    } else {
      setTitle("Create Meeting");
    }

    initializeUIComponents();
    initializeTimePicker();
  }

  private void initializeTimePicker() {
    timePickerFragment = new TimePickerFragment((view, hourOfDay, minute) -> onTimeSet(view, hourOfDay, minute), meetingStartTime);
  }

  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    meetingStartTime = LocalTime.of(hourOfDay, minute);
    updateMeetingStartTimeInView();
  }


  private void updateMeetingStartTimeInView() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    viewBinding.timePickerAction.setText(meetingStartTime.format(formatter));
    clearError(viewBinding.timePickerAction);
  }



  private void initializeUIComponents() {
    setUpPickers();
    setUpClickListeners();
    setUpMeetingLocationSelector();

    viewBinding.addLocationButton.setOnClickListener(v -> addLocation());
    viewBinding.deleteLocationButton.setOnClickListener(v -> deleteLocation());
  }

  private void addLocation() {
    String locationName = viewBinding.locationNameEditText.getText().toString().trim();
    if (!locationName.isEmpty()) {
      new Thread(() -> {
        Location existingLocation = locationDao.getLocationByName(locationName);
        if (existingLocation == null) {
          Location newLocation = new Location(locationName);
          locationDao.insertLocation(newLocation);
          runOnUiThread(() -> {
            Toast.makeText(this, "Location added successfully.", Toast.LENGTH_LONG).show();
            setUpMeetingLocationSelector();
          });
        } else {
          runOnUiThread(() -> Toast.makeText(this, "Location already exists.", Toast.LENGTH_LONG).show());
        }
      }).start();
    } else {
      Toast.makeText(this, "Please enter a location name.", Toast.LENGTH_LONG).show();
    }
  }


  private void deleteLocation() {
    if (selectedLocation != null) {
      new Thread(() -> {
        List<Meeting> meetingsAtLocation = locationDao.getMeetingsAtLocationInTimeSlot(selectedLocation.getId(), LocalDateTime.MIN, LocalDateTime.MAX);
        if (!meetingsAtLocation.isEmpty()) {
          runOnUiThread(() -> Snackbar.make(viewBinding.getRoot(), "Location cannot be deleted as it's being used for a meeting", Snackbar.LENGTH_LONG).show());
        } else {
          locationDao.deleteLocation(selectedLocation);
          runOnUiThread(() -> {
            Snackbar.make(viewBinding.getRoot(), "Location deleted successfully", Snackbar.LENGTH_LONG).show();
            setUpMeetingLocationSelector();
          });
        }
      }).start();
    } else {
      Snackbar.make(viewBinding.getRoot(), "No location selected", Snackbar.LENGTH_LONG).show();
    }
  }


  private void loadMeetingDetails(int meetingId) {
    new Thread(() -> {
      Meeting meeting = meetingDao.getMeetingById(meetingId);
      Location location = locationDao.getLocationById(meeting.getLocationId());
      runOnUiThread(() -> updateUIWithMeetingDetails(meeting, location));
    }).start();
  }

  private void updateUIWithMeetingDetails(Meeting meeting, Location location) {
    viewBinding.meetingSubjectEdit.setText(meeting.getSubject());

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    meetingDate = meeting.getDateTime().toLocalDate();
    meetingStartTime = meeting.getDateTime().toLocalTime();
    viewBinding.datePickerAction.setText(dateFormatter.format(meetingDate));
    viewBinding.timePickerAction.setText(timeFormatter.format(meetingStartTime));

    long hours = meeting.getDuration().toHours();
    long minutes = meeting.getDuration().minusHours(hours).toMinutes();
    viewBinding.durationPickerAction.setText(String.format("%d hours %d minutes", hours, minutes));

    String emails = String.join(", ", meeting.getParticipantEmailsSet());
    viewBinding.addedParticipantEmailsContainer.setText(emails);

    participantEmails.clear();
    participantEmails.addAll(meeting.getParticipantEmailsSet());

    setTitle("Edit Meeting");

    if (location != null) {
      selectedLocation = location;
      setUpMeetingLocationSelector();
    }
    if (editingMeetingId != null) {
      viewBinding.submit.setText(R.string.edit_button_text);
    } else {
      viewBinding.submit.setText(R.string.submit_button_text);
    }

  }

  private void setUpMeetingLocationSelector() {
    new Thread(() -> {
      List<Location> locations = locationDao.getAllLocations();
      runOnUiThread(() -> {
        ArrayAdapter<Location> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations);
        viewBinding.meetingLocationSelector.setAdapter(adapter);
        if (selectedLocation != null) {
          int position = adapter.getPosition(selectedLocation);
          viewBinding.meetingLocationSelector.setSelection(position);
        }

        viewBinding.meetingLocationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedLocation = (Location) parent.getItemAtPosition(position);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            selectedLocation = null;
          }
        });
      });
    }).start();
  }

  private void setUpPickers() {
    datePickerFragment = new DatePickerFragment(this::onMeetingDateSet);
    timePickerFragment = new TimePickerFragment(this::onTimeSet, LocalTime.now());
    durationPickerFragment = new DurationPickerFragment(this::onMeetingDurationSet);
  }

  private void setUpClickListeners() {
    viewBinding.datePickerAction.setOnClickListener(this::showDatePicker);
    viewBinding.timePickerAction.setOnClickListener(this::showTimePicker);
    viewBinding.durationPickerAction.setOnClickListener(this::showDurationPicker);
    viewBinding.addParticipantButton.setOnClickListener(this::addNewParticipant);
    viewBinding.submit.setOnClickListener(this::schedule);
  }

  private void showTimePicker(View v) {
    timePickerFragment.show(getSupportFragmentManager(), "timePicker");
  }

  private void showDatePicker(View v) {
    datePickerFragment.show(getSupportFragmentManager(), "meeting_date_picker");
  }

  private void onMeetingDateSet(DatePicker view, int year, int month, int day) {
    meetingDate = LocalDate.of(year, month + 1, day);
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    viewBinding.datePickerAction.setText(meetingDate.format(formatter));
    clearError(viewBinding.datePickerAction);
  }

  private void clearError(EditText editText) {
    editText.setError(null);
  }

  private void showDurationPicker(View v) {
    durationPickerFragment.show(getSupportFragmentManager(), "meeting_duration_picker");
  }

  private void onMeetingDurationSet(Duration duration) {
    meetingDuration = duration;
    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60;
    viewBinding.durationPickerAction.setText(
            getString(R.string.duration_format, hours, minutes));
    clearError(viewBinding.durationPickerAction);
  }

  private void addNewParticipant(View v) {
    String participantEmail = viewBinding.participantEmailEdit.getText().toString().trim();
    if (!participantEmail.isEmpty() && participantEmails.add(participantEmail)) {
      String prefix = participantEmails.size() > 1 ? ", " : "";
      viewBinding.addedParticipantEmailsContainer.append(prefix + participantEmail);
      viewBinding.participantEmailEdit.setText("");
    }
  }

  private void schedule(View v) {
    if (!validateForm()) return;

    new Thread(() -> {
      final String meetingSubject = viewBinding.meetingSubjectEdit.getText().toString().trim();
      final LocalDateTime meetingDateTime = LocalDateTime.of(meetingDate, meetingStartTime);
      final TimeSlot meetingSlot = new TimeSlot(meetingDateTime, meetingDuration);
      final int locationIdInt = selectedLocation.getId();

      // Check if the selected time slot and location are available
      List<Meeting> conflictingMeetings = locationDao.getMeetingsAtLocationInTimeSlot(locationIdInt, meetingDateTime, meetingDateTime.plus(meetingDuration));
      if (!conflictingMeetings.isEmpty()) {
        runOnUiThread(() -> Snackbar.make(viewBinding.getRoot(), "Error: The selected location is not available at the chosen time slot.", Snackbar.LENGTH_LONG).show());
        return;
      }

      final Meeting meeting = new Meeting(participantEmails, meetingSlot, locationIdInt, meetingSubject);
      if (editMode && editingMeetingId != null) {
        meeting.setId(editingMeetingId);
        meetingDao.updateMeeting(meeting);
        runOnUiThread(() -> {
          Snackbar.make(viewBinding.getRoot(), "Meeting updated successfully", Snackbar.LENGTH_LONG).show();
          finish();
        });
      } else {
        meetingDao.insertMeeting(meeting);
        runOnUiThread(() -> {
          Snackbar.make(viewBinding.getRoot(), "Meeting scheduled successfully", Snackbar.LENGTH_LONG).show();
          finish();
        });
      }
    }).start();
  }


  private boolean validateForm() {
    boolean isValidLocation = validateLocation();
    boolean isValidSubject = validateNotEmpty(viewBinding.meetingSubjectEdit);
    boolean isValidParticipants = validateParticipants();
    boolean isValidPickers = arePickerFieldsValid();

    return isValidLocation && isValidSubject && isValidParticipants && isValidPickers;
  }

  private boolean validateLocation() {
    if (selectedLocation == null) {
      viewBinding.meetingLocationErrorHint.setVisibility(View.VISIBLE);
      return false;
    } else {
      viewBinding.meetingLocationErrorHint.setVisibility(View.INVISIBLE);
      return true;
    }
  }

  private boolean validateNotEmpty(EditText editText) {
    if (editText.getText().toString().isEmpty()) {
      indicateError(editText);
      return false;
    }
    clearError(editText);
    return true;
  }

  private boolean validateParticipants() {
    if (participantEmails.isEmpty()) {
      indicateError(viewBinding.participantEmailEdit);
      return false;
    }
    clearError(viewBinding.participantEmailEdit);
    return true;
  }

  public boolean arePickerFieldsValid() {
    boolean isValid = true;
    if (meetingDate == null) {
      isValid = false;
      indicateError(viewBinding.datePickerAction);
    }
    if (meetingStartTime == null) {
      isValid = false;
      indicateError(viewBinding.timePickerAction);
    }
    if (meetingDuration == null || meetingDuration.isZero()) {
      isValid = false;
      indicateError(viewBinding.durationPickerAction);
    }
    return isValid;
  }

  private void indicateError(EditText editText) {
    final String errorMessage = getString(R.string.required_field_error_msg);
    editText.setHint(errorMessage);
    editText.setHintTextColor(Color.RED);
    editText.setError(errorMessage);
  }

}
