package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.ReservationResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity {

    private TextView txtPendingCount, txtApprovedCount, txtCompletedCount, txtWelcome;
    private String sessionNic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Ensure this matches your dashboard XML

        txtWelcome = findViewById(R.id.txtWelcome);
        // Ensure you have these TextViews in your activity_dashboard.xml
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtApprovedCount = findViewById(R.id.txtApprovedCount);
        txtCompletedCount = findViewById(R.id.txtCompletedCount);

        DatabaseHelper db = new DatabaseHelper(this);
        sessionNic = db.getSessionNic();

        if (sessionNic == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        txtWelcome.setText("Welcome, Prosumer " + sessionNic);

        setupBottomNavigation(R.id.nav_dashboard);
    }

    // onResume ensures metrics update automatically when backing out of a booking screen
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardMetrics();
    }

    private void loadDashboardMetrics() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.getMyReservations(sessionNic).enqueue(new Callback<List<ReservationResponse>>() {
            @Override
            public void onResponse(Call<List<ReservationResponse>> call, Response<List<ReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int pending = 0, approved = 0, completed = 0;

                    // Loop through the live reservations and increment the counters
                    for (ReservationResponse res : response.body()) {
                        if ("Pending".equalsIgnoreCase(res.getStatus())) {
                            pending++;
                        } else if ("Approved".equalsIgnoreCase(res.getStatus())) {
                            approved++;
                        } else if ("Completed".equalsIgnoreCase(res.getStatus())) {
                            completed++;
                        }
                    }

                    txtPendingCount.setText(String.valueOf(pending));
                    txtApprovedCount.setText(String.valueOf(approved));
                    txtCompletedCount.setText(String.valueOf(completed));
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load metrics", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReservationResponse>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}