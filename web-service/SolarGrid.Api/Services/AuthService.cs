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

    public AuthService(IAppUserRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<LoginResponse?> LoginAsync(LoginRequest request)
    {
        var user = await _userRepository.GetByUsernameAsync(request.Username);

        if (user == null || !user.IsActive)
            return null;

        if (user.PasswordHash != HashPassword(request.Password))
            return null;

        // Assignment note: using a GUID token rather than a signed JWT to keep
        // the dependency surface small. In production, replace with JWT Bearer.
        return new LoginResponse
        {
            Token    = Guid.NewGuid().ToString("N"),
            Username = user.Username,
            FullName = user.FullName,
            Role     = user.Role
        };
    }

    private static string HashPassword(string password)
    {
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(bytes);
    }
}
