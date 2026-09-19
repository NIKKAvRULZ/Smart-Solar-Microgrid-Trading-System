package com.team.smartsolar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.team.smartsolar.database.DatabaseHelper;
import com.team.smartsolar.models.CreateReservationRequest;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingActivity extends AppCompatActivity {

    private EditText inputStationId, inputBookingDate, inputStartTimeSlot, inputEndTimeSlot, inputEnergyAmount;
    private Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        inputStationId = findViewById(R.id.inputStationId);
        inputBookingDate = findViewById(R.id.inputBookingDate);
        inputStartTimeSlot = findViewById(R.id.inputStartTime);
        inputEndTimeSlot = findViewById(R.id.inputEndTime);
        inputEnergyAmount = findViewById(R.id.inputEnergyAmount);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Date Picker
        inputBookingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                new android.app.DatePickerDialog(CreateBookingActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            inputBookingDate.setText(selectedDate);
                        },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        // Start Time Picker
        inputStartTimeSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                new android.app.TimePickerDialog(CreateBookingActivity.this,
                        (view, hourOfDay, minute) -> {
                            String selectedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            inputStartTimeSlot.setText(selectedTime);
                        },
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE),
                        true // 24-hour format
                ).show();
            }
        });

        // End Time Picker
        inputEndTimeSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                new android.app.TimePickerDialog(CreateBookingActivity.this,
                        (view, hourOfDay, minute) -> {
                            String selectedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            inputEndTimeSlot.setText(selectedTime);
                        },
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE),
                        true // 24-hour format
                ).show();
            }
        });
        btnConfirmBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBookingSubmission();
            }
        });
    }

    private void handleBookingSubmission() {
        String station = inputStationId.getText().toString().trim();
        String date = inputBookingDate.getText().toString().trim();
        String start = inputStartTimeSlot.getText().toString().trim();
        String end = inputEndTimeSlot.getText().toString().trim();
        String amountStr = inputEnergyAmount.getText().toString().trim();

        if (station.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all booking details", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Calculate duration and convert Local Time to UTC for the backend
        int durationMinutes = 0;
        String scheduledDateTime = "";

        try {
            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date startDate = localFormat.parse(date + " " + start);
            Date endDate = localFormat.parse(date + " " + end);

            if (startDate != null && endDate != null) {
                long diffInMillis = endDate.getTime() - startDate.getTime();
                durationMinutes = (int) (diffInMillis / (1000 * 60));

                if (durationMinutes <= 0) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert the local Android time to standard UTC for C#
                SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                scheduledDateTime = utcFormat.format(startDate);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Parse Energy Amount (kWh)
        double energyAmountValue = 0.0;
        try {
            energyAmountValue = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid energy amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Get the logged-in user's NIC
        DatabaseHelper db = new DatabaseHelper(this);
        String prosumerNic = db.getSessionNic();

        if (prosumerNic == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Build the exact JSON payload the backend expects
        CreateReservationRequest request = new CreateReservationRequest(
                prosumerNic,
                station,
                scheduledDateTime,
                durationMinutes,
                energyAmountValue
        );

        // 5. Send to Server via Retrofit
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.createReservation(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateBookingActivity.this, "Reservation Confirmed!", Toast.LENGTH_SHORT).show();

                    // Route to Summary
                    Intent intent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
                    intent.putExtra("STATION", station);
                    intent.putExtra("DATE", date);
                    intent.putExtra("TIME", start + " - " + end);
                    intent.putExtra("AMOUNT", amountStr);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateBookingActivity.this, "Failed to create booking: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateBookingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}