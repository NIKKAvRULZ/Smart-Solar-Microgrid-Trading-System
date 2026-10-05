// -----------------------------------------------------------------------------
// File: UserDTOs.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for UserDTOs
// -----------------------------------------------------------------------------

namespace SolarGrid.Api.Models;

public class LoginRequest
{
    public string Username { get; set; } = null!;
    public string Password { get; set; } = null!;
}

public class LoginResponse
{
    public string Token { get; set; } = null!;
    public string Username { get; set; } = null!;
    public string FullName { get; set; } = null!;
    public UserRole Role { get; set; }
}

public class CreateUserRequest
{
    public string Username { get; set; } = null!;
    public string Password { get; set; } = null!;
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public UserRole Role { get; set; } = UserRole.GridOperator;
}

public class UpdateUserRequest
{
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public UserRole Role { get; set; }
    public bool IsActive { get; set; } = true;
}
