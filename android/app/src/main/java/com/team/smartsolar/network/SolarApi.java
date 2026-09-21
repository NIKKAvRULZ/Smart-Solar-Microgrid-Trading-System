package com.team.smartsolar.network;

import com.team.smartsolar.models.Booking;
import com.team.smartsolar.models.CreateReservationRequest;
import com.team.smartsolar.models.NodeResponse;
import com.team.smartsolar.models.RegisterRequest;
import com.team.smartsolar.models.ReservationResponse;
import com.team.smartsolar.models.Station;
import com.team.smartsolar.models.LoginRequest;   // <-- Forces it to use your model
import com.team.smartsolar.models.LoginResponse;  // <-- Forces it to use your model
import com.team.smartsolar.models.UpdateReservationRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SolarApi {

    // --- AUTHENTICATION ---
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/users")
    Call<Void> registerUser(@Body RegisterRequest request);

    // --- NODES (Map Grid) ---
    @GET("api/nodes")
    Call<List<Station>> getNodes();

    // --- RESERVATIONS ---
    @POST("api/reservations")
    Call<Void> createReservation(@Body CreateReservationRequest request);

    @GET("api/reservations/prosumer/{nic}")
    Call<java.util.List<ReservationResponse>> getMyReservations(@Path("nic") String nic);

    @PUT("api/reservations/{id}")
    Call<Void> updateReservation(@Path("id") String id, @Body UpdateReservationRequest request);

    @PATCH("api/reservations/{id}/cancel")
    Call<Void> cancelReservation(@Path("id") String id);

    // --- PROSUMERS ---
    @GET("api/prosumers/{nic}")
    Call<com.team.smartsolar.models.ProsumerProfile> getProfile(@Path("nic") String nic);

    @PUT("api/prosumers/{nic}")
    Call<Void> updateProfile(@Path("nic") String nic, @Body com.team.smartsolar.models.ProsumerProfile profile);

    @PATCH("api/prosumers/{nic}/deactivate")
    Call<Void> requestDeactivation(@Path("nic") String nic);

    @GET("api/nodes")
    Call<List<NodeResponse>> getAllStations();



}