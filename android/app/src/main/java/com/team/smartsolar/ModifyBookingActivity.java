package com.team.smartsolar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.team.smartsolar.models.UpdateReservationRequest;
import com.team.smartsolar.network.RetrofitClient;
import com.team.smartsolar.network.SolarApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyBookingActivity extends BaseActivity {

    private EditText editStationId, editBookingDate, editStartTime, editEndTime, editEnergyAmount;
    private String originalDate, originalTime, bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_booking);

        editStationId = findViewById(R.id.editStationId);
        editBookingDate = findViewById(R.id.editBookingDate);
        editStartTime = findViewById(R.id.editStartTime);
        editEndTime = findViewById(R.id.editEndTime);
        editEnergyAmount = findViewById(R.id.editEnergyAmount);

        // 1. Retrieve data passed from the adapter
        Intent intent = getIntent();
        bookingId = intent.getStringExtra("BOOKING_ID");
        editStationId.setText(intent.getStringExtra("STATION"));
        originalDate = intent.getStringExtra("DATE");
        editBookingDate.setText(originalDate);

        originalTime = intent.getStringExtra("TIME");
        if (originalTime != null && originalTime.contains("-")) {
            String[] times = originalTime.split("-");
            editStartTime.setText(times[0].trim());
            editEndTime.setText(times[1].trim());
        }
        editEnergyAmount.setText(intent.getStringExtra("ENERGY"));

        // Date Picker
        editBookingDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    editBookingDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time Pickers
        editStartTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) ->
                    editStartTime.setText(String.format("%02d:%02d", h, m)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        editEndTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) ->
                    editEndTime.setText(String.format("%02d:%02d", h, m)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // Setup Buttons connected to Live Network Methods
        findViewById(R.id.btnUpdateBooking).setOnClickListener(v -> handleUpdateBooking());
        findViewById(R.id.btnCancelBooking).setOnClickListener(v -> handleCancelBooking());

        setupBottomNavigation(R.id.nav_booking);
    }

    private void handleUpdateBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Error: Booking ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isWithin12Hours(originalDate, originalTime.split("-")[0].trim())) {
            Toast.makeText(this, "Action Denied: Less than 12 hours until original start time.", Toast.LENGTH_LONG).show();
            return;
        }

        String date = editBookingDate.getText().toString().trim();
        String start = editStartTime.getText().toString().trim();
        String end = editEndTime.getText().toString().trim();
        String amountStr = editEnergyAmount.getText().toString().trim();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

        int durationMinutes = 0;
        String scheduledDateTime = "";
        double energyAmountValue = 0.0;

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

                SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                scheduledDateTime = utcFormat.format(startDate);
            }

            energyAmountValue = Double.parseDouble(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateReservationRequest request = new UpdateReservationRequest(scheduledDateTime, durationMinutes, energyAmountValue);
        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);

        api.updateReservation(bookingId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ModifyBookingActivity.this, "Update Saved Live!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ModifyBookingActivity.this, MyBookingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ModifyBookingActivity.this, "Update failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ModifyBookingActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCancelBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Error: Booking ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isWithin12Hours(originalDate, originalTime.split("-")[0].trim())) {
            Toast.makeText(this, "Action Denied: Less than 12 hours until start time.", Toast.LENGTH_LONG).show();
            return;
        }

        SolarApi api = RetrofitClient.getClient().create(SolarApi.class);
        api.cancelReservation(bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ModifyBookingActivity.this, "Reservation Cancelled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ModifyBookingActivity.this, MyBookingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ModifyBookingActivity.this, "Cancellation failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ModifyBookingActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithin12Hours(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date bookingDate = sdf.parse(dateStr + " " + timeStr);
            if (bookingDate == null) return false;

            long differenceInMillis = bookingDate.getTime() - System.currentTimeMillis();
            long hoursDifference = differenceInMillis / (1000 * 60 * 60);

            return hoursDifference < 12;
        } catch (ParseException e) {
            return false;
        }
    }
}