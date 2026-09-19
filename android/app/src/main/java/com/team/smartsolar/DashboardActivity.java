package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.ReservationResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView txtPendingCount, txtApprovedCount, txtCompletedCount, txtWelcome;
    private String sessionNic;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        txtWelcome = findViewById(R.id.txtWelcome);
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

        // Initialize the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPlaceholder);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Example: Drop a pin in Colombo.
        // To make this dynamic, you will need to add a GET /api/nodes call via Retrofit here
        // to fetch live stations and loop through them to add markers.
        LatLng defaultStation = new LatLng(6.9271, 79.8612);
        mMap.addMarker(new MarkerOptions().position(defaultStation).title("Microgrid Station 1"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultStation, 12));
    }
}