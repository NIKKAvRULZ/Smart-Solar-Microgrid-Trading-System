package com.team.smartsolar.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SolarApi {

    // This tells Retrofit to make a GET request to Sasmitha's endpoint
    @GET("api/v1/Stations")
    Call<Object> getStations(); // We will swap 'Object' for a real Station model next

}