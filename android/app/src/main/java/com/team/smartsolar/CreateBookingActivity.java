package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateBookingActivity extends AppCompatActivity {

    private EditText inputStationId, inputBookingDate, inputTimeSlot, inputEnergyAmount;
    private Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        inputStationId = findViewById(R.id.inputStationId);
        inputBookingDate = findViewById(R.id.inputBookingDate);
        inputTimeSlot = findViewById(R.id.inputTimeSlot);
        inputEnergyAmount = findViewById(R.id.inputEnergyAmount);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        btnConfirmBooking.setOnClickListener(v -> handleBookingSubmission());

        // --- Setup Bottom Navigation ---
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_booking); // Highlight Booking tab

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    startActivity(new android.content.Intent(CreateBookingActivity.this, DashboardActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_booking) {
                    return true; // Already here
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new android.content.Intent(CreateBookingActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }

    }

    private void handleBookingSubmission() {
        String station = inputStationId.getText().toString().trim();
        String date = inputBookingDate.getText().toString().trim();
        String time = inputTimeSlot.getText().toString().trim();
        String amountStr = inputEnergyAmount.getText().toString().trim();

        // 1. Basic Validation
        if (station.isEmpty() || date.isEmpty() || time.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all booking details", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * 2. Mock API Call
         * Later, this will be a POST /api/reservations request via Retrofit.
         * The C# backend will enforce the 7-day rule and check capacity here.
         */

        Toast.makeText(this, "Booking confirmed locally!", Toast.LENGTH_SHORT).show();

        // 3. Navigate to Summary Screen and pass the data along
        Intent intent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
        intent.putExtra("STATION", station);
        intent.putExtra("DATE", date);
        intent.putExtra("TIME", time);
        intent.putExtra("AMOUNT", amountStr);
        startActivity(intent);

        finish(); // Close the booking form so the back button doesn't reload it
    }
}