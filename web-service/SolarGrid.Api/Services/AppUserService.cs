using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Full CRUD service for web-application users (Backoffice / Grid Operators).
/// Managed exclusively by Backoffice officers through the Users page.
/// </summary>
public class AppUserService
{
    private readonly IAppUserRepository _userRepository;
    private readonly IProsumerRepository _prosumerRepository;

    public AppUserService(IAppUserRepository userRepository, IProsumerRepository prosumerRepository)
    {
        _userRepository = userRepository;
        _prosumerRepository = prosumerRepository;
    }

    public async Task<List<AppUser>> GetAllAsync() =>
        await _userRepository.GetAllAsync();

    /// <summary>Returns null if the username is already taken.</summary>
    public async Task<AppUser?> CreateAsync(CreateUserRequest request)
    {
        var existing = await _userRepository.GetByUsernameAsync(request.Username);
        if (existing != null) return null;

        var user = new AppUser
        {
            Username = request.Username,
            PasswordHash = HashPassword(request.Password),
            FullName = request.FullName,
            Email = request.Email,
            Role = request.Role,
            IsActive = true
        };

        await _userRepository.CreateAsync(user);

        // Auto-sync: Create a matching profile in the Prosumers collection
        if (string.Equals(request.Role, "Prosumer", StringComparison.OrdinalIgnoreCase))
        {
            var existingProsumer = await _prosumerRepository.GetByNicAsync(request.Username);
            if (existingProsumer == null)
            {
                var prosumer = new Prosumer
                {
                    Nic = request.Username,
                    FullName = request.FullName,
                    Email = request.Email,
                    Phone = "",
                    Address = "",
                    PasswordHash = user.PasswordHash,
                    IsActive = true
                };
                await _prosumerRepository.CreateAsync(prosumer);
            }
        }

        return user;
    }

    /// <summary>Returns null if the user id is not found.</summary>
    public async Task<AppUser?> UpdateAsync(string id, UpdateUserRequest request)
    {
        var user = await _userRepository.GetByIdAsync(id);
        if (user == null) return null;

        user.FullName = request.FullName;
        user.Email = request.Email;
        user.Role = request.Role;
        user.IsActive = request.IsActive;

        await _userRepository.UpdateAsync(id, user);
        return user;
    }

    public async Task<bool> DeactivateAsync(string id)
    {
        var user = await _userRepository.GetByIdAsync(id);
        if (user == null) return false;

        user.IsActive = false;
        await _userRepository.UpdateAsync(id, user);
        return true;
    }

    private static string HashPassword(string password)
    {
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(bytes);
    }
}