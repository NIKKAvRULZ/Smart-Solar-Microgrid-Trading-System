package com.team.smartsolar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText editProfileNic, editProfileName, editProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editProfileNic = findViewById(R.id.editProfileNic);
        editProfileName = findViewById(R.id.editProfileName);
        editProfileEmail = findViewById(R.id.editProfileEmail);

        // Load mock data (This will later come from GET /api/prosumers/{nic})
        editProfileNic.setText("991234567V");
        editProfileName.setText("Nithika Perera");
        editProfileEmail.setText("nithika@example.com");

        Button btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        Button btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);

        // Handle Profile Update
        btnUpdateProfile.setOnClickListener(v -> {
            String updatedName = editProfileName.getText().toString().trim();
            String updatedEmail = editProfileEmail.getText().toString().trim();

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                // Mock API Call (PUT /api/prosumers/{nic})
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Deactivation Request
        btnDeactivateAccount.setOnClickListener(v -> showDeactivationDialog());

        // --- Setup Logout Button ---
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            com.team.smartsolar.database.DatabaseHelper db = new com.team.smartsolar.database.DatabaseHelper(this);
            db.logoutUser();

            // Go back to login and clear the activity history
            android.content.Intent intent = new android.content.Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // --- Setup Bottom Navigation ---
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile); // Highlight Profile tab

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new android.content.Intent(ProfileActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_booking) {
                startActivity(new android.content.Intent(ProfileActivity.this, CreateBookingActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Already here
            }
            return false;
        });

    }

    private void showDeactivationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deactivation")
                .setMessage("Are you sure you want to deactivate your account? This will block future logins and reservations.")
                .setPositiveButton("Yes, Deactivate", (dialog, which) -> {
                    // Mock API Call (POST /api/prosumers/{nic}/deactivation-request)
                    Toast.makeText(this, "Deactivation request submitted to Backoffice.", Toast.LENGTH_LONG).show();
                    finish(); // Close profile screen
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}