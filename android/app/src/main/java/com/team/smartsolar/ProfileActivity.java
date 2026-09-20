package com.team.smartsolar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.ProsumerProfile;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private TextView txtProfileNic, txtProfileStatus;
    private TextInputEditText inputProfileName, inputProfileEmail, inputProfilePhone, inputProfileAddress;
    private Button btnUpdateProfile, btnDeactivateAccount, btnLogout;
    private String sessionNic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtProfileNic = findViewById(R.id.txtProfileNic);
        txtProfileStatus = findViewById(R.id.txtProfileStatus);

        inputProfileName = findViewById(R.id.inputProfileName);
        inputProfileEmail = findViewById(R.id.inputProfileEmail);
        inputProfilePhone = findViewById(R.id.inputProfilePhone);
        inputProfileAddress = findViewById(R.id.inputProfileAddress);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);
        btnLogout = findViewById(R.id.btnLogout);

        DatabaseHelper db = new DatabaseHelper(this);
        sessionNic = db.getSessionNic();

        if (sessionNic == null) {
            Toast.makeText(this, "Session expired.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtProfileNic.setText("NIC ID: " + sessionNic);

        loadLiveProfileData();

        btnUpdateProfile.setOnClickListener(v -> handleProfileUpdate());
        btnDeactivateAccount.setOnClickListener(v -> handleDeactivationRequest());

        btnLogout.setOnClickListener(v -> {
            db.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation(R.id.nav_profile);
    }

    private void loadLiveProfileData() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        DatabaseHelper db = new DatabaseHelper(this);

        api.getProfile(sessionNic).enqueue(new Callback<ProsumerProfile>() {
            @Override
            public void onResponse(Call<ProsumerProfile> call, Response<ProsumerProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProsumerProfile profile = response.body();
                    db.cacheProfile(sessionNic, profile); // 1. Save fresh data to local SQLite
                    populateProfileUI(profile);
                } else {
                    loadOfflineProfile(db);
                }
            }

            @Override
            public void onFailure(Call<ProsumerProfile> call, Throwable t) {
                loadOfflineProfile(db); // 2. Trigger fallback on network error
            }
        });
    }

    private void loadOfflineProfile(DatabaseHelper db) {
        ProsumerProfile cachedProfile = db.getCachedProfile(sessionNic);
        if (cachedProfile != null) {
            Toast.makeText(this, "Offline Mode: Showing cached profile", Toast.LENGTH_SHORT).show();
            populateProfileUI(cachedProfile);
        } else {
            Toast.makeText(this, "Network Error: No offline data available", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateProfileUI(ProsumerProfile profile) {
        if (inputProfileName != null) inputProfileName.setText(profile.getFullName());
        if (inputProfileEmail != null) inputProfileEmail.setText(profile.getEmail());
        if (inputProfilePhone != null) inputProfilePhone.setText(profile.getPhone());
        if (inputProfileAddress != null) inputProfileAddress.setText(profile.getAddress());
        updateStatusBadge(profile.isActive());
    }

    private void updateStatusBadge(boolean isActive) {
        if (isActive) {
            txtProfileStatus.setText("Status: ACTIVE");
            txtProfileStatus.setTextColor(Color.parseColor("#10B981")); // Green
            txtProfileStatus.setBackgroundColor(Color.parseColor("#2010B981"));
        } else {
            txtProfileStatus.setText("Status: DEACTIVATED");
            txtProfileStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            txtProfileStatus.setBackgroundColor(Color.parseColor("#20EF4444"));
        }
    }

    private void handleProfileUpdate() {
        String name = inputProfileName.getText() != null ? inputProfileName.getText().toString().trim() : "";
        String email = inputProfileEmail.getText() != null ? inputProfileEmail.getText().toString().trim() : "";
        String phone = inputProfilePhone.getText() != null ? inputProfilePhone.getText().toString().trim() : "";
        String address = inputProfileAddress.getText() != null ? inputProfileAddress.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        ProsumerProfile updateRequest = new ProsumerProfile(name, email, phone, address);
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);

        api.updateProfile(sessionNic, updateRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeactivationRequest() {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.requestDeactivation(sessionNic).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Account deactivated", Toast.LENGTH_LONG).show();
                    updateStatusBadge(false);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to deactivate", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}