// -----------------------------------------------------------------------------
// File: AuthService.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for AuthService
// -----------------------------------------------------------------------------

using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Handles login for web-application users (Backoffice and Grid Operators).
/// Returns a session token on success; null on bad credentials or deactivated account.
/// </summary>
public class AuthService
{
    private readonly IAppUserRepository _userRepository;
    private readonly IProsumerRepository _prosumerRepository;

    public AuthService(IAppUserRepository userRepository, IProsumerRepository prosumerRepository)
    {
        _userRepository = userRepository;
        _prosumerRepository = prosumerRepository;
    }

    public async Task<LoginResponse?> LoginAsync(LoginRequest request)
    {
        // Inline comment: Method execution begins here.
        // 1. Try Backoffice / Grid Operator (AppUsers)
        var user = await _userRepository.GetByUsernameAsync(request.Username);
        if (user != null)
        {
            if (!user.IsActive || user.PasswordHash != HashPassword(request.Password))
                return null;

            return new LoginResponse
            {
                Token    = Guid.NewGuid().ToString("N"),
                Username = user.Username,
                FullName = user.FullName,
                Role     = user.Role
            };
        }

        // 2. Try Prosumers
        var prosumer = await _prosumerRepository.GetByNicAsync(request.Username);
        if (prosumer != null)
        {
            if (!prosumer.IsActive || prosumer.PasswordHash != HashPassword(request.Password))
                return null;

            return new LoginResponse
            {
                Token    = Guid.NewGuid().ToString("N"),
                Username = prosumer.Nic,
                FullName = prosumer.FullName,
                Role     = UserRole.Prosumer
            };
        }

        return null;
    }

    private static string HashPassword(string password)
    {
        // Inline comment: Method execution begins here.
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(bytes);
    }
}
