package com.team.smartsolar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModifyBookingActivity extends BaseActivity {

    private EditText editStationId, editBookingDate, editStartTime, editEndTime, editEnergyAmount;
    private String originalDate, originalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_booking);

        editStationId = findViewById(R.id.editStationId);
        editBookingDate = findViewById(R.id.editBookingDate);
        editStartTime = findViewById(R.id.editStartTime);
        editEndTime = findViewById(R.id.editEndTime);
        editEnergyAmount = findViewById(R.id.editEnergyAmount);

        // 1. Retrieve data passed from the adapter
        Intent intent = getIntent();
        editStationId.setText(intent.getStringExtra("STATION"));
        originalDate = intent.getStringExtra("DATE");
        editBookingDate.setText(originalDate);

        originalTime = intent.getStringExtra("TIME"); // e.g., "10:00 - 11:00"
        if (originalTime != null && originalTime.contains("-")) {
            String[] times = originalTime.split("-");
            editStartTime.setText(times[0].trim());
            editEndTime.setText(times[1].trim());
        }
        editEnergyAmount.setText(intent.getStringExtra("ENERGY"));

        // Date Picker
        editBookingDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    editBookingDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time Pickers
        editStartTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) ->
                    editStartTime.setText(String.format("%02d:%02d", h, m)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        editEndTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) ->
                    editEndTime.setText(String.format("%02d:%02d", h, m)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // Setup Buttons
        findViewById(R.id.btnUpdateBooking).setOnClickListener(v -> handleAction("UPDATE"));
        findViewById(R.id.btnCancelBooking).setOnClickListener(v -> handleAction("CANCEL"));

        setupBottomNavigation(R.id.nav_booking);
    }

    private void handleAction(String actionType) {
        String station = editStationId.getText().toString().trim();
        String newDate = editBookingDate.getText().toString().trim();
        String newStart = editStartTime.getText().toString().trim();
        String newEnd = editEndTime.getText().toString().trim();
        String newEnergy = editEnergyAmount.getText().toString().trim();
        String newTimeCombined = newStart + " - " + newEnd;

        // Enforce the 12-Hour Rule against the original booking time
        if (isWithin12Hours(originalDate, editStartTime.getText().toString().trim())) {
            android.widget.Toast.makeText(this, "Action Denied: Less than 12 hours until start time.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        // Connect to local database
        com.team.smartsolar.database.DatabaseHelper db = new com.team.smartsolar.database.DatabaseHelper(this);

        if (actionType.equals("UPDATE")) {
            // Apply updates to the database
            db.updateMockBooking(station, originalDate, originalTime, newDate, newTimeCombined, newEnergy);
            android.widget.Toast.makeText(this, "Booking Updated Successfully!", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            // Remove from database
            db.deleteMockBooking(station, originalDate, originalTime);
            android.widget.Toast.makeText(this, "Booking Cancelled.", android.widget.Toast.LENGTH_SHORT).show();
        }

        // Return to MyBookings screen
        android.content.Intent intent = new android.content.Intent(this, MyBookingsActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // Evaluates if the current time is within 12 hours of the booking start time
    private boolean isWithin12Hours(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date bookingDate = sdf.parse(dateStr + " " + timeStr);
            if (bookingDate == null) return false;

            long differenceInMillis = bookingDate.getTime() - System.currentTimeMillis();
            long hoursDifference = differenceInMillis / (1000 * 60 * 60);

            return hoursDifference < 12;
        } catch (ParseException e) {
            return false;
        }
    }
}