package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.team.smartsolar.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    // Declare variables for our UI elements
    private EditText inputNic, inputName, inputEmail, inputPassword;
    private Button btnRegister;
    private TextView txtGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Links this Java file to the XML layout

        // 1. Initialize UI Elements by finding their IDs from the XML
        inputNic = findViewById(R.id.inputNic);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtGoToLogin = findViewById(R.id.txtGoToLogin);

        // 2. Set Click Listener for the Register Button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        // 3. Set Click Listener for the "Go To Login" text
        txtGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Placeholder: We will add an Intent to open LoginActivity here later
                Toast.makeText(RegisterActivity.this, "Navigate to Login clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 4. Method to extract data and validate
    private void handleRegistration() {
        // Extract text and remove extra spaces
        String nic = inputNic.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Basic Validation: Ensure no fields are empty
        if (nic.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return; // Stop execution if validation fails
        }

        // Basic Validation: Ensure NIC is somewhat valid (you can adjust this rule)
        if (nic.length() < 9) {
            Toast.makeText(this, "Please enter a valid NIC", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- MOCK REGISTRATION BYPASS ---
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // 1. Save the mock user profile to SQLite
        dbHelper.saveUserProfile(nic, name, email, "Prosumer", "Active");

        // 2. Save a fake login session
        dbHelper.saveSession(nic, "Prosumer", "mock_token_12345");

        // 3. Show success and navigate to Dashboard
        Toast.makeText(this, "Mock Registration Success!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();

        /*
         * TODO: Next Steps for API Integration
         * 1. Package these variables into a JSON object.
         * 2. Send to Sasmitha's C# API endpoint (e.g., POST /api/prosumers/register) using Retrofit.
         * 3. Handle the success/error response.
         */

        // For now, show a success message to prove the data is collected
        Toast.makeText(this, "Data ready to send for NIC: " + nic, Toast.LENGTH_LONG).show();
    }
}