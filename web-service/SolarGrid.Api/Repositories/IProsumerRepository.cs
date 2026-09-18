using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface IProsumerRepository
{
    Task<List<Prosumer>> GetAllAsync();
    Task<Prosumer?> GetByNicAsync(string nic);
    Task CreateAsync(Prosumer prosumer);
    Task UpdateAsync(string nic, Prosumer prosumer);
}
