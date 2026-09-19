package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class UpdateReservationRequest {
    @SerializedName("scheduledDateTime")
    private String scheduledDateTime;

    @SerializedName("durationMinutes")
    private int durationMinutes;

    @SerializedName("energyAmount")
    private double energyAmount;

    public UpdateReservationRequest(String scheduledDateTime, int durationMinutes, double energyAmount) {
        this.scheduledDateTime = scheduledDateTime;
        this.durationMinutes = durationMinutes;
        this.energyAmount = energyAmount;
    }
}