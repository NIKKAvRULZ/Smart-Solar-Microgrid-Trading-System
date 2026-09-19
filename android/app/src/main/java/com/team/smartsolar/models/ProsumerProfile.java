package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class ProsumerProfile {
    @SerializedName("nic")
    private String nic;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("status")
    private String status;

    // Constructor used for sending updates (NIC and Status are read-only on the server)
    public ProsumerProfile(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getNic() { return nic; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}