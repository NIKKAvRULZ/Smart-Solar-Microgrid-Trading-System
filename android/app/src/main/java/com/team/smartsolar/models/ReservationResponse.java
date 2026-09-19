package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class ReservationResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("scheduledDateTime")
    private String scheduledDateTime;

    @SerializedName("durationMinutes")
    private int durationMinutes;

    @SerializedName("energyAmount")
    private double energyAmount;


    @SerializedName("status")
    private String status;

    public String getId() { return id; }
    public String getNodeId() { return nodeId; }
    public String getScheduledDateTime() { return scheduledDateTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }
    public double getEnergyAmount() { return energyAmount; }

}