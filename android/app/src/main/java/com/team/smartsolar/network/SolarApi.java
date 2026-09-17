package com.team.smartsolar.network;

import com.team.smartsolar.models.Station;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SolarApi {
    @GET("api/v1/Stations")
    Call<List<Station>> getStations();
}