package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.CreateReservationRequest;
import com.team.smartsolar.models.NodeResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingActivity extends BaseActivity {

    private Spinner spinnerStation;
    private EditText inputBookingDate, inputStartTimeSlot, inputEndTimeSlot, inputEnergyAmount;
    private Button btnConfirmBooking;

    // Store the live list of stations so we can look up the ID later
    private List<NodeResponse> liveStations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        spinnerStation = findViewById(R.id.spinnerStation);
        inputBookingDate = findViewById(R.id.inputBookingDate);
        inputStartTimeSlot = findViewById(R.id.inputStartTime);
        inputEndTimeSlot = findViewById(R.id.inputEndTime);
        inputEnergyAmount = findViewById(R.id.inputEnergyAmount);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Fetch the active nodes to populate the dropdown
        loadStationsIntoSpinner();

        // Date Picker
        inputBookingDate.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(CreateBookingActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        inputBookingDate.setText(selectedDate);
                    },
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Start Time Picker
        inputStartTimeSlot.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            new android.app.TimePickerDialog(CreateBookingActivity.this,
                    (view, hourOfDay, minute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        inputStartTimeSlot.setText(selectedTime);
                    },
                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE),
                    true
            ).show();
        });

        // End Time Picker
        inputEndTimeSlot.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            new android.app.TimePickerDialog(CreateBookingActivity.this,
                    (view, hourOfDay, minute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        inputEndTimeSlot.setText(selectedTime);
                    },
                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE),
                    true
            ).show();
        });

        btnConfirmBooking.setOnClickListener(v -> handleBookingSubmission());

        setupBottomNavigation(R.id.nav_booking);
    }

    private void loadStationsIntoSpinner() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.getAllStations().enqueue(new Callback<List<NodeResponse>>() {
            @Override
            public void onResponse(Call<List<NodeResponse>> call, Response<List<NodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveStations = response.body();
                    List<String> stationNames = new ArrayList<>();

                    for (NodeResponse station : liveStations) {
                        String cleanName = station.getName() != null ? station.getName().trim() : "Unknown";

                        if (station.getAvailableBatterySlots() > 0 && station.getCapacityKWh() > 0) {
                            stationNames.add(cleanName + " (" + station.getAvailableBatterySlots() + " slots | " + station.getCapacityKWh() + " kWh)");
                        } else {
                            stationNames.add(cleanName + " (STATION FULL)");
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateBookingActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, stationNames);
                    spinnerStation.setAdapter(adapter);

                    // NEW: Auto-select the station if the user came from the Grid Explorer Map
                    String preselectedId = getIntent().getStringExtra("PRESELECTED_STATION_ID");
                    if (preselectedId != null) {
                        for (int i = 0; i < liveStations.size(); i++) {
                            if (preselectedId.equals(liveStations.get(i).getStationId())) {
                                spinnerStation.setSelection(i);
                                break; // Stop looping once we find it
                            }
                        }
                    }

                } else {
                    Toast.makeText(CreateBookingActivity.this, "Failed to load stations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<NodeResponse>> call, Throwable t) {
                Toast.makeText(CreateBookingActivity.this, "Network error loading stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBookingSubmission() {
        if (liveStations.isEmpty() || spinnerStation.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "No valid station selected", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = spinnerStation.getSelectedItemPosition();
        NodeResponse selectedStation = liveStations.get(selectedIndex);
        String selectedStationId = selectedStation.getStationId();
        String selectedStationName = selectedStation.getStationName().trim();

        // Prevent booking if the station is physically full
        if (selectedStation.getAvailableBatterySlots() <= 0) {
            Toast.makeText(this, "This station has no available slots.", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = inputBookingDate.getText().toString().trim();
        String start = inputStartTimeSlot.getText().toString().trim();
        String end = inputEndTimeSlot.getText().toString().trim();
        String amountStr = inputEnergyAmount.getText().toString().trim();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all booking details", Toast.LENGTH_SHORT).show();
            return;
        }

        int durationMinutes = 0;
        String scheduledDateTime = "";

        try {
            java.text.SimpleDateFormat localFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            java.util.Date startDate = localFormat.parse(date + " " + start);
            java.util.Date endDate = localFormat.parse(date + " " + end);

            if (startDate != null && endDate != null) {
                long diffInMillis = endDate.getTime() - startDate.getTime();
                durationMinutes = (int) (diffInMillis / (1000 * 60));

                if (durationMinutes <= 0) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                java.text.SimpleDateFormat utcFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                utcFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                scheduledDateTime = utcFormat.format(startDate);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        double energyAmountValue = 0.0;
        try {
            energyAmountValue = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid energy amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent booking if they request more energy than the station has left
        if (energyAmountValue > selectedStation.getCapacityKWh()) {
            Toast.makeText(this, "Station only has " + selectedStation.getCapacityKWh() + " kWh available.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        String prosumerNic = db.getSessionNic();

        if (prosumerNic == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateReservationRequest request = new CreateReservationRequest(
                prosumerNic,
                selectedStationId,
                scheduledDateTime,
                durationMinutes,
                energyAmountValue
        );

        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);

        api.createReservation(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateBookingActivity.this, "Reservation Confirmed!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
                    intent.putExtra("STATION", selectedStationName);
                    intent.putExtra("DATE", date);
                    intent.putExtra("TIME", start + " - " + end);
                    intent.putExtra("AMOUNT", amountStr);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateBookingActivity.this, "Failed to create booking: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateBookingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}