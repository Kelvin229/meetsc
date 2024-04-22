package com.kelvin229.meetsc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kelvin229.meetsc.databinding.ActivityMeetingListBinding;
import com.kelvin229.meetsc.domain.models.AppDatabase;
import com.kelvin229.meetsc.domain.models.Location;
import com.kelvin229.meetsc.domain.models.LocationDao;
import com.kelvin229.meetsc.domain.models.MeetingDao;
import com.kelvin229.meetsc.domain.models.MeetingWithLocation;
import com.kelvin229.meetsc.events.EditMeetingEvent;
import com.kelvin229.meetsc.ui.fragments.pickers.DatePickerFragment;
import com.kelvin229.meetsc.ui.meetings_list.MeetingsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeetingsListActivity extends AppCompatActivity {

    private ActivityMeetingListBinding viewBinding;
    private MeetingsListAdapter meetingsListAdapter;
    private DatePickerFragment datePickerFragment;

    private LocationDao locationDao;
    private MeetingDao meetingDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMeetingListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        meetingDao = AppDatabase.getDatabase(getApplicationContext()).meetingDao();
        locationDao = AppDatabase.getDatabase(getApplicationContext()).locationDao();

        setUpRecyclerView();
        viewBinding.scheduleMeetingButton.setOnClickListener(this::startScheduleMeetingActivity);
        datePickerFragment = new DatePickerFragment(this::onMeetingFilteredByDate);

        updateMeetingList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMeetingList();
    }
    private void updateMeetingList() {
        new Thread(() -> {
            List<MeetingWithLocation> updatedMeetings = meetingDao.getMeetingsWithLocationNames();
            runOnUiThread(() -> meetingsListAdapter.updateMeetings(updatedMeetings));
        }).start();
    }


    private void setUpRecyclerView() {
        meetingsListAdapter = new MeetingsListAdapter(new ArrayList<>());
        viewBinding.activityMainMeetingRecyclerview.setAdapter(meetingsListAdapter);
        viewBinding.activityMainMeetingRecyclerview.setLayoutManager(new LinearLayoutManager(this));
    }

    private void startScheduleMeetingActivity(View button){
        startActivity(new Intent(this, ScheduleMeetingActivity.class));
    }

    private void onMeetingFilteredByDate(DatePicker view, int year, int month, int day) {
        final LocalDate date = LocalDate.of(year, month + 1, day);
        new Thread(() -> {
            List<MeetingWithLocation> filteredMeetings = meetingDao.getMeetingsWithLocationNamesByDate(date.toString());
            runOnUiThread(() -> meetingsListAdapter.updateMeetings(filteredMeetings));
        }).start();
    }

    private void onMeetingFilteredByLocation(int locationId){
        new Thread(() -> {
            List<MeetingWithLocation> filteredMeetings = meetingDao.getMeetingsWithLocationNamesByLocationId(locationId);
            runOnUiThread(() -> meetingsListAdapter.updateMeetings(filteredMeetings));
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.filter_by_date) {
            datePickerFragment.show(getSupportFragmentManager(), "filter_date_picker");
        } else if (itemId == R.id.reset_filter) {
            resetMeetingList();
        } else if (itemId == R.id.filter_by_location) {
            showLocationSelectorDialog();
        }
        else if (itemId == R.id.delete_all_meetings) {
            deleteAllMeetings();
        }
        else if (itemId == R.id.delete_by_date) {
            deleteMeetingsByDate();
        }
        else if (itemId == R.id.delete_by_location) {
            deleteMeetingsByLocation();
        } else {
            throw new IllegalStateException("Unexpected value: " + itemId);
        }
        return super.onOptionsItemSelected(item);
    }


    private void showLocationSelectorDialog() {
        new Thread(() -> {
            List<Integer> locationIds = getAllLocationIds();
            String[] locationNames = new String[locationIds.size()];
            for (int i = 0; i < locationIds.size(); i++) {
                locationNames[i] = getLocationNameById(locationIds.get(i));
            }

            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.choose_location)
                        .setItems(locationNames, (dialog, which) -> onMeetingFilteredByLocation(locationIds.get(which)))
                        .show();
            });
        }).start();
    }

    private void deleteAllMeetings() {
        new Thread(() -> {
            meetingDao.deleteAllMeetings();
            runOnUiThread(this::updateMeetingList);
        }).start();
    }

    private void deleteMeetingsByDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                    String dateString = date.toString();

                    new Thread(() -> {
                        meetingDao.deleteMeetingsByDate(dateString);
                        runOnUiThread(this::updateMeetingList);
                    }).start();
                },
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue() - 1,
                LocalDate.now().getDayOfMonth());
        datePickerDialog.show();
    }

    private void deleteMeetingsByLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_location);

        new Thread(() -> {
            List<Integer> locationIds = getAllLocationIds();
            String[] locationNames = new String[locationIds.size()];
            for (int i = 0; i < locationIds.size(); i++) {
                locationNames[i] = getLocationNameById(locationIds.get(i));
            }

            runOnUiThread(() -> {
                builder.setItems(locationNames, (dialog, which) -> {
                    new Thread(() -> {
                        meetingDao.deleteMeetingsByLocationId(locationIds.get(which));
                        runOnUiThread(this::updateMeetingList);
                    }).start();
                });

                builder.show();
            });
        }).start();
    }

    private List<Integer> getAllLocationIds() {
        List<Location> locations = locationDao.getAllLocations();
        List<Integer> locationIds = new ArrayList<>();
        for (Location location : locations) {
            locationIds.add(location.getId());
        }
        return locationIds;
    }

    private String getLocationNameById(int id) {
        Location location = locationDao.getLocationById(id);
        return location != null ? location.getName() : null;
    }
    private void resetMeetingList() {
        updateMeetingList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEditMeetingEvent(EditMeetingEvent event) {
        Intent intent = new Intent(this, ScheduleMeetingActivity.class);
        intent.putExtra("editMode", true);
        intent.putExtra("meetingId", event.getMeetingId());
        startActivity(intent);
    }
}