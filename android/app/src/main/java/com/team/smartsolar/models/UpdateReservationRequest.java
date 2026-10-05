// -----------------------------------------------------------------------------
// File: UpdateReservationRequest.java
// Author: Nithika Perera
// Purpose: DTO packaging modified reservation details (date, time, energy) to
// send to the API while complying with the 12-hour modification rule.
// -----------------------------------------------------------------------------

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