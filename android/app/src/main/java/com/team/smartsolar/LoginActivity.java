// -----------------------------------------------------------------------------
// File: LoginActivity.java
// Author: Nithika Perera
// Purpose: Handles Prosumer authentication, capturing credentials and storing
// the issued JWT token securely in the local SQLite database for session persistence.
// -----------------------------------------------------------------------------

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
import com.team.smartsolar.models.LoginRequest;
import com.team.smartsolar.models.LoginResponse;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText inputLoginNic, inputLoginPassword;
    private Button btnLogin;
    private TextView txtGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputLoginNic = findViewById(R.id.inputLoginNic);
        inputLoginPassword = findViewById(R.id.inputLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoToRegister = findViewById(R.id.txtGoToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nic = inputLoginNic.getText().toString().trim();
                String password = inputLoginPassword.getText().toString().trim();

                if (nic.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter NIC and Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. Create the request body
                LoginRequest request = new LoginRequest(nic, password);

                // 2. Call the API
                SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
                api.login(request).enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().getToken();
                            String role = response.body().getRole();

                            // Save the real JWT token to SQLite
                            DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this);
                            dbHelper.saveSession(nic, role, token);

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Route to the dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish(); // Close login screen
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Switches the screen to RegisterActivity
        txtGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}