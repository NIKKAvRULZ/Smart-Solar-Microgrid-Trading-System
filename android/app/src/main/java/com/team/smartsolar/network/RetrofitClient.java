package com.team.smartsolar.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 10.0.2.2 is the Android Emulator alias for your computer's "localhost"
    // When testing on a physical phone later, change this to Sasmitha's Wi-Fi IP (e.g., 192.168.1.15)
    private static final String BASE_URL = "http://10.0.2.2:5107/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}