package com.team.smartsolar.network;

import com.team.smartsolar.models.Booking;
import com.team.smartsolar.models.RegisterRequest;
import com.team.smartsolar.models.Station;
import com.team.smartsolar.models.LoginRequest;   // <-- Forces it to use your model
import com.team.smartsolar.models.LoginResponse;  // <-- Forces it to use your model

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface SolarApi {

    // --- AUTHENTICATION ---
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // --- NODES (Map Grid) ---
    @GET("api/nodes")
    Call<List<Station>> getNodes();

    // --- RESERVATIONS ---
    @POST("api/reservations")
    Call<Void> createReservation(@Body Booking request);

    @GET("api/reservations/prosumer/{nic}")
    Call<List<Booking>> getMyReservations(@Path("nic") String nic);

    @PATCH("api/reservations/{id}/cancel")
    Call<Void> cancelReservation(@Path("id") String id);

    @POST("api/users")
    Call<Void> registerUser(@Body RegisterRequest request);
}