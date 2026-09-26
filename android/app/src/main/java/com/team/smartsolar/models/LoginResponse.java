// -----------------------------------------------------------------------------
// File: LoginResponse.java
// Author: Nithika Perera
// Purpose: Data Transfer Object (DTO) for parsing the authentication response
// containing the JWT and role from the central C# Web API.
// -----------------------------------------------------------------------------

package com.team.smartsolar.models;

public class LoginResponse {
    private String token;
    private String role;

    public String getToken() { return token; }
    public String getRole() { return role; }
}