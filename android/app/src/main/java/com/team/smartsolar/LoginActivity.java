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
                } else {
                    // --- MOCK LOGIN BYPASS ---
                    // 1. Save a fake session to our local SQLite DB
                    DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this);
                    dbHelper.saveSession(nic, "Prosumer", "mock_token_12345");

                    // 2. Show success message
                    Toast.makeText(LoginActivity.this, "Mock Login Success!", Toast.LENGTH_SHORT).show();

                    // 3. Navigate to the Dashboard
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);

                    // 4. Close the login screen so the back button doesn't bring them back here
                    finish();
                }
            }
        });

        // This switches the screen to RegisterActivity
        txtGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}