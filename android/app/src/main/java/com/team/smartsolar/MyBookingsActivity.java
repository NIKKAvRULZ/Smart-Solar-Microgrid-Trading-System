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

        // 1. Setup the Route to Create Booking Form
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
        // Fetch all saved bookings from SQLite
        DatabaseHelper db = new DatabaseHelper(this);
        List<Booking> savedBookings = db.getMockBookings();

        // Bind the data to the UI
        adapter = new BookingAdapter(savedBookings);
        recyclerView.setAdapter(adapter);
    }
}