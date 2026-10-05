// -----------------------------------------------------------------------------
// File: RegisterRequest.java
// Author: Nithika Perera
// Purpose: DTO used to package Prosumer registration inputs into a JSON payload
// to be sent to the central C# Web API.
// -----------------------------------------------------------------------------

package com.team.smartsolar.models;

public class RegisterRequest {
    private String nic;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;

    public RegisterRequest(String nic, String password, String fullName, String email, String phone, String address) {
        this.nic = nic;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}