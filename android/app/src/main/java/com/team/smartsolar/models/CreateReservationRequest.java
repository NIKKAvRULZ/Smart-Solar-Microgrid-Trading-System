// -----------------------------------------------------------------------------
// File: CreateReservationRequest.java
// Author: Nithika Perera
// Purpose: DTO packaging new reservation parameters for submission to the
// central API, subject to the 7-day advance booking window rule.
// -----------------------------------------------------------------------------

package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class CreateReservationRequest {
    @SerializedName("prosumerNic")
    private String prosumerNic;

    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("scheduledDateTime")
    private String scheduledDateTime;

    @SerializedName("durationMinutes")
    private int durationMinutes;

    @SerializedName("energyAmount")
    private double energyAmount;

    public CreateReservationRequest(String prosumerNic, String nodeId, String scheduledDateTime, int durationMinutes, double energyAmount) {
        this.prosumerNic = prosumerNic;
        this.nodeId = nodeId;
        this.scheduledDateTime = scheduledDateTime;
        this.durationMinutes = durationMinutes;
        this.energyAmount = energyAmount;
    }
}