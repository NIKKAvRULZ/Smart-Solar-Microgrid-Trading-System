package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.team.smartsolar.models.Station;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPlaceholder);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- Setup Refresh Stations Button ---
        Button btnViewStations = findViewById(R.id.btnViewStations);
        btnViewStations.setOnClickListener(v -> {
            if (mMap != null) {
                Toast.makeText(this, "Fetching live data...", Toast.LENGTH_SHORT).show();
                fetchStationsFromApi();
            } else {
                Toast.makeText(this, "Map is still loading", Toast.LENGTH_SHORT).show();
            }
        });

        // --- NEW: Setup Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                return true;
            }
            else if (itemId == R.id.nav_booking) {
                startActivity(new Intent(DashboardActivity.this, CreateBookingActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Center map over Sri Lanka (Malabe region)
        LatLng defaultLocation = new LatLng(6.9147, 79.9724);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
    }

    // --- Retrofit Network Call ---
    private void fetchStationsFromApi() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        Call<List<Station>> call = api.getStations();

        call.enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mMap.clear();

                    List<Station> stations = response.body();
                    for (Station station : stations) {
                        LatLng position = new LatLng(station.getLatitude(), station.getLongitude());

                        mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(station.getName())
                                .snippet("Capacity: " + station.getCapacity() + " kW"));
                    }
                    Toast.makeText(DashboardActivity.this, "Loaded " + stations.size() + " stations", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load data. Is API running?", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}