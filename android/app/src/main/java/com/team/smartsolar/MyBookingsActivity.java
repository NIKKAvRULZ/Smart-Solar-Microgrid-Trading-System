package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team.smartsolar.adapters.BookingAdapter;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.Booking;

import java.util.List;

public class MyBookingsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        // 1. Set up the Route to Create Booking Form
        Button btnGoToCreateBooking = findViewById(R.id.btnGoToCreateBooking);
        btnGoToCreateBooking.setOnClickListener(v -> {
            startActivity(new Intent(MyBookingsActivity.this, CreateBookingActivity.class));
        });

        // 2. Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Setup Bottom Navigation
        setupBottomNavigation(R.id.nav_booking);
    }

    // onResume fires every time this screen comes to the foreground
    @Override
    protected void onResume() {
        super.onResume();
        loadBookingsFromDatabase();
    }

    private void loadBookingsFromDatabase() {
        com.team.smartsolar.database.DatabaseHelper db = new com.team.smartsolar.database.DatabaseHelper(this);
        String nic = db.getSessionNic();

        if (nic == null) {
            android.widget.Toast.makeText(this, "Session expired. Please log in.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.team.smartsolar.network.SolarApi api = com.team.smartsolar.network.RetrofitClient.getClient().create(com.team.smartsolar.network.SolarApi.class);
        api.getMyReservations(nic).enqueue(new retrofit2.Callback<java.util.List<com.team.smartsolar.models.ReservationResponse>>() {

            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.team.smartsolar.models.ReservationResponse>> call, retrofit2.Response<java.util.List<com.team.smartsolar.models.ReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<com.team.smartsolar.models.Booking> mappedBookings = new java.util.ArrayList<>();

                    for (com.team.smartsolar.models.ReservationResponse res : response.body()) {
                        String displayDate = "";
                        String displayTime = "";

                        try {
                            String serverTime = res.getScheduledDateTime();
                            java.util.Date parsedDate = null;

                            // Try to parse C# DateTime with milliseconds first
                            try {
                                java.text.SimpleDateFormat isoFormatMs = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                isoFormatMs.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                parsedDate = isoFormatMs.parse(serverTime);
                            } catch (Exception e1) {
                                // Fallback for C# DateTime without milliseconds
                                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                                isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                parsedDate = isoFormat.parse(serverTime);
                            }

                            // Convert the UTC Date back to Local Time for display
                            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            dateFormat.setTimeZone(java.util.TimeZone.getDefault()); // User's local timezone
                            displayDate = dateFormat.format(parsedDate);

                            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                            timeFormat.setTimeZone(java.util.TimeZone.getDefault());
                            String startTime = timeFormat.format(parsedDate);

                            // Calculate End Time using durationMinutes
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(parsedDate);
                            cal.add(java.util.Calendar.MINUTE, res.getDurationMinutes());
                            String endTime = timeFormat.format(cal.getTime());

                            displayTime = startTime + " - " + endTime;

                        } catch (Exception e) {
                            displayDate = "Format Error";
                            displayTime = "Format Error";
                        }

                        // Map backend data back to the frontend Booking model
                        mappedBookings.add(new com.team.smartsolar.models.Booking(
                                res.getId(),
                                res.getNodeId(),
                                displayDate,
                                displayTime,
                                String.valueOf(res.getEnergyAmount()),
                                res.getStatus()
                        ));
                    }

                    adapter = new com.team.smartsolar.adapters.BookingAdapter(mappedBookings);
                    recyclerView.setAdapter(adapter);
                } else {
                    android.widget.Toast.makeText(MyBookingsActivity.this, "Failed to fetch bookings.", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.team.smartsolar.models.ReservationResponse>> call, Throwable t) {
                android.widget.Toast.makeText(MyBookingsActivity.this, "Network error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}