using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface IStationRepository
{
    Task<List<SolarStationInfo>> GetAllAsync();
    Task<SolarStationInfo?> GetByIdAsync(string id);
    Task CreateAsync(SolarStationInfo station);
}
