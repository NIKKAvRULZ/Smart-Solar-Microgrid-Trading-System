// -----------------------------------------------------------------------------
// File: StationMapActivity.java
// Author: Nithika Perera
// Purpose: Prosumer Grid Explorer map. Fetches microgrid nodes from the API
// (or local SQLite cache) and renders them on Google Maps. Includes a bottom
// sheet to view capacity and jump straight into booking.
// -----------------------------------------------------------------------------

package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.NodeResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final Map<Marker, NodeResponse> markerStationMap = new HashMap<>();
    private final Map<String, Marker> stationIdMarkerMap = new HashMap<>();
    private final List<NodeResponse> stationList = new ArrayList<>();

    private MaterialCardView cardStationDetails;
    private TextView txtSheetStationName, txtSheetSlots, txtSheetEnergy;
    private RecyclerView recyclerStations;
    private StationCardAdapter adapter;
    private NodeResponse currentlySelectedStation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_map);

        findViewById(R.id.btnBackFromMap).setOnClickListener(v -> finish());

        cardStationDetails = findViewById(R.id.cardStationDetails);
        txtSheetStationName = findViewById(R.id.txtSheetStationName);
        txtSheetSlots = findViewById(R.id.txtSheetSlots);
        txtSheetEnergy = findViewById(R.id.txtSheetEnergy);

        findViewById(R.id.btnSheetBook).setOnClickListener(v -> {
            Intent intent = new Intent(StationMapActivity.this, CreateBookingActivity.class);
            if (currentlySelectedStation != null) {
                // Pass the ID to the booking page
                intent.putExtra("PRESELECTED_STATION_ID", currentlySelectedStation.getStationId());
            }
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnZoomToStations).setOnClickListener(v -> zoomToAllStations());

        recyclerStations = findViewById(R.id.recyclerStations);
        recyclerStations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new StationCardAdapter(stationList, this::focusOnStation);
        recyclerStations.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fullMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure UI controls
        UiSettings ui = mMap.getUiSettings();
        ui.setZoomControlsEnabled(true);        // Visual + / - buttons on the right edge
        ui.setCompassEnabled(true);             // Compass displays automatically when rotated
        ui.setRotateGesturesEnabled(true);      // Two-finger twist to rotate map
        ui.setTiltGesturesEnabled(true);        // Two-finger slide to tilt 3D perspective
        ui.setScrollGesturesEnabled(true);
        ui.setZoomGesturesEnabled(true);

        // Adjust map control margins so buttons don't clip into the top header or cards
        mMap.setPadding(0, 220, 0, 320);

        mMap.setOnMapClickListener(latLng -> cardStationDetails.setVisibility(View.GONE));

        mMap.setOnMarkerClickListener(marker -> {
            NodeResponse station = markerStationMap.get(marker);
            if (station != null) {
                displayStationDetails(station);
            }
            return false;
        });

        loadStations();
    }

    private void loadStations() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        DatabaseHelper db = new DatabaseHelper(this);

        api.getAllStations().enqueue(new Callback<List<NodeResponse>>() {
            @Override
            public void onResponse(Call<List<NodeResponse>> call, Response<List<NodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    db.cacheStations(response.body()); // 1. Save grid nodes to local SQLite
                    populateMapAndList(response.body());
                } else {
                    loadOfflineStations(db);
                }
            }

            @Override
            public void onFailure(Call<List<NodeResponse>> call, Throwable t) {
                loadOfflineStations(db); // 2. Trigger fallback on network error
            }
        });
    }

    private void loadOfflineStations(DatabaseHelper db) {
        List<NodeResponse> cachedStations = db.getCachedStations();
        if (!cachedStations.isEmpty()) {
            Toast.makeText(this, "Offline Mode: Showing cached stations", Toast.LENGTH_SHORT).show();
            populateMapAndList(cachedStations);
        } else {
            Toast.makeText(this, "Network error loading stations", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateMapAndList(List<NodeResponse> stations) {
        stationList.clear();
        markerStationMap.clear();
        stationIdMarkerMap.clear();

        if (mMap != null) mMap.clear(); // Clear old pins

        stationList.addAll(stations);
        adapter.notifyDataSetChanged();

        for (NodeResponse station : stationList) {
            LatLng loc = new LatLng(station.getLatitude(), station.getLongitude());
            String stationName = station.getStationName() != null ? station.getStationName() : station.getName();
            String stationId = station.getStationId() != null ? station.getStationId() : station.getId();

            if (mMap != null) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(stationName));
                if (marker != null) {
                    markerStationMap.put(marker, station);
                    stationIdMarkerMap.put(stationId, marker);
                }
            }
        }
        zoomToAllStations();
    }

    private void focusOnStation(NodeResponse station) {
        if (mMap == null || station == null) return;

        LatLng loc = new LatLng(station.getLatitude(), station.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));

        Marker marker = stationIdMarkerMap.get(station.getStationId());
        if (marker != null) {
            marker.showInfoWindow();
        }

        displayStationDetails(station);
    }

    private void displayStationDetails(NodeResponse station) {
        currentlySelectedStation = station; // Track for booking

        String cleanName = station.getStationName() != null ? station.getStationName().trim() : "Unknown Station";
        txtSheetStationName.setText(cleanName);
        txtSheetSlots.setText(String.valueOf(station.getAvailableBatterySlots()));
        txtSheetEnergy.setText(station.getCapacityKWh() + " kWh");
        cardStationDetails.setVisibility(View.VISIBLE);
    }

    private void zoomToAllStations() {
        if (markerStationMap.isEmpty() || mMap == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markerStationMap.keySet()) {
            builder.include(marker.getPosition());
        }

        try {
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));
        } catch (Exception e) {
            Marker first = markerStationMap.keySet().iterator().next();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(first.getPosition(), 11f));
        }
    }

    // Horizontal List Adapter
    private static class StationCardAdapter extends RecyclerView.Adapter<StationCardAdapter.ViewHolder> {
        private final List<NodeResponse> list;
        private final OnStationClickListener listener;

        interface OnStationClickListener {
            void onStationClick(NodeResponse station);
        }

        StationCardAdapter(List<NodeResponse> list, OnStationClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            NodeResponse st = list.get(pos);
            h.txtName.setText(st.getStationName() != null ? st.getStationName().trim() : "Station");
            h.txtSlots.setText(st.getAvailableBatterySlots() + " slots");
            h.txtEnergy.setText(st.getCapacityKWh() + " kWh");
            h.itemView.setOnClickListener(v -> listener.onStationClick(st));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtSlots, txtEnergy;
            ViewHolder(View v) {
                super(v);
                txtName = v.findViewById(R.id.txtCardStationName);
                txtSlots = v.findViewById(R.id.txtCardSlots);
                txtEnergy = v.findViewById(R.id.txtCardEnergy);
            }
        }
    }
}