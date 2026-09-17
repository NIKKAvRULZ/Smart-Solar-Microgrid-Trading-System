package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.team.smartsolar.database.DatabaseHelper;

public class ProfileActivity extends BaseActivity {

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
            DatabaseHelper db = new DatabaseHelper(this);
            db.logoutUser();

            // Go back to login and clear the activity history
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // --- Setup Bottom Navigation ---
        setupBottomNavigation(R.id.nav_profile);

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