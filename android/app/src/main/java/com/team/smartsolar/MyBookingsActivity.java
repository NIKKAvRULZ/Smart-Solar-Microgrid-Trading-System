package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team.smartsolar.adapters.BookingAdapter;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.Booking;
import com.team.smartsolar.models.ReservationResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private Spinner spinnerFilterStatus;
    private java.util.Map<String, String> stationMap = new java.util.HashMap<>();

    // Master list from the server
    private List<ReservationResponse> allServerReservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        Button btnGoToCreateBooking = findViewById(R.id.btnGoToCreateBooking);
        btnGoToCreateBooking.setOnClickListener(v -> {
            startActivity(new Intent(MyBookingsActivity.this, CreateBookingActivity.class));
        });

        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerFilterStatus = findViewById(R.id.spinnerFilterStatus);
        setupFilterSpinner();

        // Change nav_booking to match your bottom nav ID if necessary
        setupBottomNavigation(R.id.nav_booking);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookingsFromDatabase();
    }

    private void setupFilterSpinner() {
        String[] statuses = new String[]{"All", "Pending", "Approved", "Completed", "Cancelled"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerFilterStatus.setAdapter(spinnerAdapter);

        spinnerFilterStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = statuses[position];
                applyFilter(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadBookingsFromDatabase() {
        DatabaseHelper db = new DatabaseHelper(this);
        String nic = db.getSessionNic();

        if (nic == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Load cached stations and map their IDs to their readable names
        stationMap.clear();
        for (com.team.smartsolar.models.NodeResponse node : db.getCachedStations()) {
            String id = node.getId() != null ? node.getId() : node.getStationId();
            String name = node.getName() != null ? node.getName() : node.getStationName();
            if (id != null && name != null) {
                stationMap.put(id, name);
            }
        }

        // 2. Fetch the user's reservations
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.getMyReservations(nic).enqueue(new Callback<List<ReservationResponse>>() {
            @Override
            public void onResponse(Call<List<ReservationResponse>> call, Response<List<ReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allServerReservations = response.body();
                    applyFilter(spinnerFilterStatus.getSelectedItem().toString());
                } else {
                    Toast.makeText(MyBookingsActivity.this, "Failed to fetch bookings.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReservationResponse>> call, Throwable t) {
                Toast.makeText(MyBookingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filters the master list locally and updates the RecyclerView
    private void applyFilter(String statusFilter) {
        List<Booking> filteredBookings = new ArrayList<>();

        for (ReservationResponse res : allServerReservations) {
            if (statusFilter.equals("All") || statusFilter.equalsIgnoreCase(res.getStatus())) {
                String displayDate = "";
                String displayTime = "";

                try {
                    String serverTime = res.getScheduledDateTime();
                    Date parsedDate = null;

                    try {
                        SimpleDateFormat isoFormatMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        isoFormatMs.setTimeZone(TimeZone.getTimeZone("UTC"));
                        parsedDate = isoFormatMs.parse(serverTime);
                    } catch (Exception e1) {
                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        parsedDate = isoFormat.parse(serverTime);
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getDefault());
                    displayDate = dateFormat.format(parsedDate);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    timeFormat.setTimeZone(TimeZone.getDefault());
                    String startTime = timeFormat.format(parsedDate);

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(parsedDate);
                    cal.add(java.util.Calendar.MINUTE, res.getDurationMinutes());
                    String endTime = timeFormat.format(cal.getTime());

                    displayTime = startTime + " - " + endTime;

                } catch (Exception e) {
                    displayDate = "Format Error";
                    displayTime = "Format Error";
                }

                // Look up the real name from the dictionary, fallback to a shortened ID if not found
                String realStationName = stationMap.containsKey(res.getNodeId())
                        ? stationMap.get(res.getNodeId())
                        : "Station " + res.getNodeId().substring(0, 6) + "...";

                filteredBookings.add(new Booking(
                        res.getId(),
                        realStationName, // <-- This passes the readable name instead of res.getNodeId()
                        displayDate,
                        displayTime,
                        String.valueOf(res.getEnergyAmount()),
                        res.getStatus()
                ));
            }
        }

        adapter = new BookingAdapter(filteredBookings);
        recyclerView.setAdapter(adapter);
    }
}