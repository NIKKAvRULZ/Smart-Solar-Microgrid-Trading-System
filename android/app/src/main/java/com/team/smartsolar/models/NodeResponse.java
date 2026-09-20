package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class NodeResponse {

    @SerializedName("id") // C# returns "id"
    private String id;

    @SerializedName("name") // C# returns "name"
    private String name;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("capacityKWh")
    private double capacityKWh;

    @SerializedName("totalBatterySlots")
    private int totalBatterySlots;

    @SerializedName("availableBatterySlots")
    private int availableBatterySlots;

    @SerializedName("isActive")
    private boolean isActive;

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getCapacityKWh() { return capacityKWh; }
    public int getTotalBatterySlots() { return totalBatterySlots; }
    public int getAvailableBatterySlots() { return availableBatterySlots; }
    public boolean isActive() { return isActive; }

    // Convenience aliases so existing code does not break
    public String getStationId() { return id; }
    public String getStationName() { return name != null ? name : "Station " + id; }
}