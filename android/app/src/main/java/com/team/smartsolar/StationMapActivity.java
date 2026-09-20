package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.team.smartsolar.models.NodeResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Map<Marker, NodeResponse> markerStationMap = new HashMap<>();

    private MaterialCardView cardStationDetails;
    private TextView txtSheetStationName, txtSheetSlots, txtSheetEnergy;

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
            startActivity(new Intent(StationMapActivity.this, CreateBookingActivity.class));
            finish();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fullMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> cardStationDetails.setVisibility(View.GONE));

        mMap.setOnMarkerClickListener(marker -> {
            NodeResponse station = markerStationMap.get(marker);
            if (station != null) {
                String cleanName = station.getStationName() != null ? station.getStationName().trim() : "Unknown Station";
                txtSheetStationName.setText(cleanName);

                txtSheetSlots.setText(String.valueOf(station.getAvailableBatterySlots()));
                txtSheetEnergy.setText(station.getCapacityKWh() + " kWh");

                cardStationDetails.setVisibility(View.VISIBLE);
            }
            return false;
        });

        loadStations();
    }

    private void loadStations() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.getAllStations().enqueue(new Callback<List<NodeResponse>>() {
            @Override
            public void onResponse(Call<List<NodeResponse>> call, Response<List<NodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NodeResponse station : response.body()) {
                        LatLng loc = new LatLng(station.getLatitude(), station.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(station.getStationName()));
                        markerStationMap.put(marker, station);
                    }
                    if (!response.body().isEmpty()) {
                        LatLng firstLoc = new LatLng(response.body().get(0).getLatitude(), response.body().get(0).getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, 10));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NodeResponse>> call, Throwable t) {
                Toast.makeText(StationMapActivity.this, "Network error loading stations", Toast.LENGTH_SHORT).show();
            }
        });
    }
}