using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Manages prosumer accounts — solar property owners who trade energy on the
/// grid via the mobile app. Registered and managed by Backoffice officers.
/// </summary>
public class ProsumerService
{
    private readonly IProsumerRepository _prosumerRepository;

    public ProsumerService(IProsumerRepository prosumerRepository)
    {
        _prosumerRepository = prosumerRepository;
    }

    public async Task<List<Prosumer>> GetAllAsync() =>
        await _prosumerRepository.GetAllAsync();
    public async Task<Prosumer?> GetByNicAsync(string nic) =>
        await _prosumerRepository.GetByNicAsync(nic);

    /// <summary>Returns null if a prosumer with that NIC already exists.</summary>
    public async Task<Prosumer?> CreateAsync(CreateProsumerRequest request)
    {
        var existing = await _prosumerRepository.GetByNicAsync(request.Nic);
        if (existing != null) return null;

        var prosumer = new Prosumer
        {
            Nic          = request.Nic,
            FullName     = request.FullName,
            Email        = request.Email,
            Phone        = request.Phone,
            Address      = request.Address,
            PasswordHash = HashPassword(request.Password),
            IsActive     = true
        };

        await _prosumerRepository.CreateAsync(prosumer);
        return prosumer;
    }

    public async Task<Prosumer?> UpdateAsync(string nic, UpdateProsumerRequest request)
    {
        var prosumer = await _prosumerRepository.GetByNicAsync(nic);
        if (prosumer == null) return null;

        prosumer.FullName = request.FullName;
        prosumer.Email    = request.Email;
        prosumer.Phone    = request.Phone;
        prosumer.Address  = request.Address;

        await _prosumerRepository.UpdateAsync(nic, prosumer);
        return prosumer;
    }

    public async Task<bool> DeactivateAsync(string nic)
    {
        var prosumer = await _prosumerRepository.GetByNicAsync(nic);
        if (prosumer == null) return false;

        prosumer.IsActive = false;
        await _prosumerRepository.UpdateAsync(nic, prosumer);
        return true;
    }

    /// <summary>Reactivation is a Backoffice-only action.</summary>
    public async Task<bool> ReactivateAsync(string nic)
    {
        var prosumer = await _prosumerRepository.GetByNicAsync(nic);
        if (prosumer == null) return false;

        prosumer.IsActive = true;
        await _prosumerRepository.UpdateAsync(nic, prosumer);
        return true;
    }

    private static string HashPassword(string password)
    {
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(bytes);
    }
}
