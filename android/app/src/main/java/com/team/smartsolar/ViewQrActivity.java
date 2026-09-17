package com.team.smartsolar;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ViewQrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qr);

        ImageView imgQrCode = findViewById(R.id.imgQrCode);
        TextView txtQrDetails = findViewById(R.id.txtQrDetails);
        Button btnCloseQr = findViewById(R.id.btnCloseQr);

        // 1. Retrieve the booking data passed from the list
        String station = getIntent().getStringExtra("STATION");
        String date = getIntent().getStringExtra("DATE");
        String energy = getIntent().getStringExtra("ENERGY");

        txtQrDetails.setText(station + "\n" + date + " | " + energy + " kWh");

        // 2. Format the payload that Sasmitha's API will verify later
        String payload = "SMARTSOLAR|" + station + "|" + date + "|" + energy;

        // 3. Generate the QR Code Image
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Create a 400x400 pixel QR code
            Bitmap bitmap = barcodeEncoder.encodeBitmap(payload, BarcodeFormat.QR_CODE, 400, 400);
            imgQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnCloseQr.setOnClickListener(v -> finish());
    }
}