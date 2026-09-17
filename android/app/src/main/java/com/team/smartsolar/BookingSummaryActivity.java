package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class BookingSummaryActivity extends BaseActivity {

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
        setupBottomNavigation(R.id.nav_booking);
    }
}