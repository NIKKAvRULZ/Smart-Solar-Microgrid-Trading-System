package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.team.smartsolar.models.NodeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView txtPendingCount, txtApprovedCount, txtCompletedCount, txtWelcome;
    private String sessionNic;
    private GoogleMap mMap;

    // Dictionary to hold live station names for the Next Booking Widget
    private java.util.Map<String, String> stationMap = new java.util.HashMap<>();

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

        // Set the personalized greeting directly
        if (txtWelcome != null) {
            txtWelcome.setText("Welcome, Nithika");
        }

        setupBottomNavigation(R.id.nav_dashboard);

        // Safely hook up Quick Actions to prevent NullPointerExceptions
        Button btnQuickBook = findViewById(R.id.btnQuickBook);
        if (btnQuickBook != null) {
            btnQuickBook.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, CreateBookingActivity.class)));
        }

        Button btnQuickHistory = findViewById(R.id.btnQuickHistory);
        if (btnQuickHistory != null) {
            btnQuickHistory.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MyBookingsActivity.class)));
        }

        View mapClickOverlay = findViewById(R.id.mapClickOverlay);
        if (mapClickOverlay != null) {
            mapClickOverlay.setOnClickListener(v -> {
                startActivity(new Intent(DashboardActivity.this, StationMapActivity.class));
            });
        }
        // Initialize the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPlaceholder);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnViewStations = findViewById(R.id.btnViewStations);
        if (btnViewStations != null) {
            btnViewStations.setOnClickListener(v -> {
                if (mMap != null) {
                    mMap.clear();
                    onMapReady(mMap);
                    Toast.makeText(DashboardActivity.this, "Refreshing grid stations...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardMetrics();
    }

    private void loadDashboardMetrics() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        DatabaseHelper db = new DatabaseHelper(this);

        // 1. Fetch stations to map their names for the widget
        api.getAllStations().enqueue(new Callback<List<NodeResponse>>() {
            @Override
            public void onResponse(Call<List<NodeResponse>> call, Response<List<NodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    db.cacheStations(response.body()); // Keep cache fresh
                    mapStationsToDictionary(response.body());
                } else {
                    mapStationsToDictionary(db.getCachedStations()); // Fallback
                }
                fetchReservationsAndUpdateDashboard(api);
            }

            @Override
            public void onFailure(Call<List<NodeResponse>> call, Throwable t) {
                mapStationsToDictionary(db.getCachedStations()); // Fallback on error
                fetchReservationsAndUpdateDashboard(api);
            }
        });
    }

    private void mapStationsToDictionary(List<NodeResponse> stations) {
        stationMap.clear();
        for (NodeResponse node : stations) {
            String id = node.getId() != null ? node.getId() : node.getStationId();
            String name = node.getName() != null ? node.getName() : node.getStationName();
            if (id != null && name != null) {
                stationMap.put(id, name);
            }
        }
    }

    private void fetchReservationsAndUpdateDashboard(SolarApi api) {
        api.getMyReservations(sessionNic).enqueue(new Callback<List<ReservationResponse>>() {
            @Override
            public void onResponse(Call<List<ReservationResponse>> call, Response<List<ReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int pending = 0, approved = 0, completed = 0;
                    ReservationResponse nextBooking = null;

                    for (ReservationResponse res : response.body()) {
                        if ("Pending".equalsIgnoreCase(res.getStatus())) {
                            pending++;
                        } else if ("Approved".equalsIgnoreCase(res.getStatus())) {
                            approved++;
                            // Grab the first approved booking to feature on the dashboard
                            if (nextBooking == null) nextBooking = res;
                        } else if ("Completed".equalsIgnoreCase(res.getStatus())) {
                            completed++;
                        }
                    }

                    // Safely update UI
                    if (txtPendingCount != null) txtPendingCount.setText(String.valueOf(pending));
                    if (txtApprovedCount != null) txtApprovedCount.setText(String.valueOf(approved));
                    if (txtCompletedCount != null) txtCompletedCount.setText(String.valueOf(completed));

                    // Update the Next Booking Card
                    TextView txtNextStation = findViewById(R.id.txtNextStation);
                    TextView txtNextTime = findViewById(R.id.txtNextTime);

                    if (txtNextStation != null && txtNextTime != null) {
                        if (nextBooking != null) {
                            String realStationName = stationMap.containsKey(nextBooking.getNodeId())
                                    ? stationMap.get(nextBooking.getNodeId())
                                    : "Station ID: " + nextBooking.getNodeId().substring(0, 6);

                            txtNextStation.setText(realStationName);

                            // Clean up the ISO date string for display (e.g., 2026-09-25T14:30:00Z -> 2026-09-25)
                            try {
                                String rawDate = nextBooking.getScheduledDateTime();
                                String displayDate = rawDate.contains("T") ? rawDate.split("T")[0] : rawDate;
                                txtNextTime.setText(displayDate + " | " + nextBooking.getEnergyAmount() + " kWh Reserved");
                            } catch (Exception e) {
                                txtNextTime.setText(nextBooking.getEnergyAmount() + " kWh Reserved");
                            }

                        } else {
                            txtNextStation.setText("No upcoming bookings");
                            txtNextTime.setText("Ready to book a microgrid slot?");
                        }
                    }
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
        mMap.getUiSettings().setAllGesturesEnabled(false);
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.getAllStations().enqueue(new Callback<List<NodeResponse>>() {
            @Override
            public void onResponse(Call<List<NodeResponse>> call, Response<List<NodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NodeResponse station : response.body()) {
                        LatLng location = new LatLng(station.getLatitude(), station.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(location).title(station.getStationName()));
                    }

                    if (!response.body().isEmpty()) {
                        LatLng firstLoc = new LatLng(response.body().get(0).getLatitude(), response.body().get(0).getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, 10));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NodeResponse>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Failed to load map markers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}