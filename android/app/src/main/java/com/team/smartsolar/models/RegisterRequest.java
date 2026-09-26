// -----------------------------------------------------------------------------
// File: RegisterRequest.java
// Author: Nithika Perera
// Purpose: DTO used to package Prosumer registration inputs into a JSON payload
// to be sent to the central C# Web API.
// -----------------------------------------------------------------------------

package com.team.smartsolar.models;

public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;

    public RegisterRequest(String username, String password, String fullName, String email, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
}