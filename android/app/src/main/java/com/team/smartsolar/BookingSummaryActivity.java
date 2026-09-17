package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookingSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        TextView txtSummaryStation = findViewById(R.id.txtSummaryStation);
        TextView txtSummaryDate = findViewById(R.id.txtSummaryDate);
        TextView txtSummaryTime = findViewById(R.id.txtSummaryTime);
        TextView txtSummaryAmount = findViewById(R.id.txtSummaryAmount);
        Button btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        // 1. Retrieve the data passed from CreateBookingActivity
        Intent intent = getIntent();
        String station = intent.getStringExtra("STATION");
        String date = intent.getStringExtra("DATE");
        String time = intent.getStringExtra("TIME");
        String amount = intent.getStringExtra("AMOUNT");

        // 2. Display the data
        txtSummaryStation.setText("Station: " + (station != null ? station : "N/A"));
        txtSummaryDate.setText("Date: " + (date != null ? date : "N/A"));
        txtSummaryTime.setText("Time: " + (time != null ? time : "N/A"));
        txtSummaryAmount.setText("Energy Reserved: " + (amount != null ? amount : "0") + " kWh");

        // 3. Navigate safely back to the Dashboard
        btnBackToDashboard.setOnClickListener(v -> {
            Intent dashboardIntent = new Intent(BookingSummaryActivity.this, DashboardActivity.class);
            // This flag clears the backstack so the user doesn't accidentally hit 'back' into a completed booking
            dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(dashboardIntent);
            finish();
        });

        // --- Setup Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Safety check: ensure the nav bar actually exists in the XML layout before setting it up
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_booking); // Highlight Booking tab

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    // FIXED: Changed context to BookingSummaryActivity.this
                    startActivity(new Intent(BookingSummaryActivity.this, DashboardActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_booking) {
                    return true; // Already here
                } else if (itemId == R.id.nav_profile) {
                    // FIXED: Changed context to BookingSummaryActivity.this
                    startActivity(new Intent(BookingSummaryActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
}