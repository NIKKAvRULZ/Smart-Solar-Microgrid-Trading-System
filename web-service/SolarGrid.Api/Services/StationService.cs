using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

public class StationService
{
    private readonly IStationRepository _stationRepository;

    public StationService(IStationRepository stationRepository)
    {
        _stationRepository = stationRepository;
    }

    public async Task<List<SolarStationInfo>> GetStationsAsync()
    {
        // Add business logic here if needed (e.g., filtering out inactive stations if not requested by backoffice)
        return await _stationRepository.GetAllAsync();
    }

    public async Task<SolarStationInfo?> GetStationAsync(string id)
    {
        return await _stationRepository.GetByIdAsync(id);
    }

    public async Task CreateStationAsync(SolarStationInfo newStation)
    {
        // Business logic validations can be applied here
        await _stationRepository.CreateAsync(newStation);
    }
}
