package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class Station {

    // @SerializedName maps the exact C# JSON keys to your Java variables
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("powerCapacityKw")
    private double powerCapacityKw;

    @SerializedName("status")
    private String status;

    // Getters so the Map can read the data
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getPowerCapacityKw() { return powerCapacityKw; }
    public String getStatus() { return status; }
}