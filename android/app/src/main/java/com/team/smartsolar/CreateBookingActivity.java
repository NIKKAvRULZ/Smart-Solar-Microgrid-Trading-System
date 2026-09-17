package com.team.smartsolar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class CreateBookingActivity extends BaseActivity {

    private EditText inputStationId, inputBookingDate, inputStartTimeSlot, inputEndTimeSlot, inputEnergyAmount;
    private Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        inputStationId = findViewById(R.id.inputStationId);
        inputBookingDate = findViewById(R.id.inputBookingDate);
        inputStartTimeSlot = findViewById(R.id.inputStartTime);
        inputEndTimeSlot = findViewById(R.id.inputEndTime);
        inputEnergyAmount = findViewById(R.id.inputEnergyAmount);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Date Picker
        inputBookingDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(CreateBookingActivity.this, (view, year, month, dayOfMonth) -> {
                // Formats the date as YYYY-MM-DD
                inputBookingDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Start Time Picker
        inputStartTimeSlot.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(CreateBookingActivity.this, (view, hourOfDay, minute) -> {
                inputStartTimeSlot.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // End Time Picker
        inputEndTimeSlot.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(CreateBookingActivity.this, (view, hourOfDay, minute) -> {
                inputEndTimeSlot.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        btnConfirmBooking.setOnClickListener(v -> handleBookingSubmission());

        // --- Setup Bottom Navigation ---
        setupBottomNavigation(R.id.nav_booking);
    }

    private void handleBookingSubmission() {
        String station = inputStationId.getText().toString().trim();
        String date = inputBookingDate.getText().toString().trim();
        String start = inputStartTimeSlot.getText().toString().trim();
        String end = inputEndTimeSlot.getText().toString().trim();
        String amountStr = inputEnergyAmount.getText().toString().trim();

        // 1. Basic Validation
        if (station.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all booking details", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Save to SQLite Cache
        com.team.smartsolar.database.DatabaseHelper db = new com.team.smartsolar.database.DatabaseHelper(this);
        db.saveMockBooking(station, date, start, end, amountStr);

        Toast.makeText(this, "Booking saved locally!", Toast.LENGTH_SHORT).show();

        // 3. Navigate to Summary Screen and pass the data along
        Intent intent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
        intent.putExtra("STATION", station);
        intent.putExtra("DATE", date);
        intent.putExtra("TIME", start + " - " + end); // Combine start and end for the receipt
        intent.putExtra("AMOUNT", amountStr);
        startActivity(intent);

        finish(); // Close the booking form so the back button doesn't reload it
    }
}