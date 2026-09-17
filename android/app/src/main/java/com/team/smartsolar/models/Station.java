package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class Station {
    @SerializedName("stationId")
    private String stationId;

    @SerializedName("name")
    private String name;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("capacity")
    private double capacity;

    @SerializedName("availableSlots")
    private int availableSlots;

    @SerializedName("status")
    private String status;

    public String getStationId() { return stationId; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getCapacity() { return capacity; }
    public int getAvailableSlots() { return availableSlots; }
    public String getStatus() { return status; }
}