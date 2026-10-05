package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        Button btnScanQr = findViewById(R.id.btnScanQr);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnScanQr.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(OperatorDashboardActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan Prosumer's QR Ticket");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        });

        btnLogout.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(this);
            db.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String bookingId = result.getContents();
                completeReservation(bookingId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void completeReservation(String bookingId) {
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.completeReservation(bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OperatorDashboardActivity.this, "Energy Transfer Authorized & Completed!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OperatorDashboardActivity.this, "Failed to complete. May be invalid or already processed.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OperatorDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
