// -----------------------------------------------------------------------------
// File: LoginRequest.java
// Author: Nithika Perera
// Purpose: DTO for securely transmitting Prosumer credentials (NIC and password)
// to the authentication endpoint of the central service.
// -----------------------------------------------------------------------------

package com.team.smartsolar.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    // Forces Retrofit to send this as "username" in the JSON body
    @SerializedName("username")
    private String nic;

    @SerializedName("password")
    private String password;

    public LoginRequest(String nic, String password) {
        this.nic = nic;
        this.password = password;
    }
}