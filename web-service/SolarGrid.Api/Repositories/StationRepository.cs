using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public class StationRepository : IStationRepository
{
    private readonly IMongoCollection<SolarStationInfo> _stationsCollection;

    public StationRepository(
        IMongoClient mongoClient,
        IOptions<SolarGridDatabaseSettings> settings)
    {
        var database = mongoClient.GetDatabase(settings.Value.DatabaseName);
        _stationsCollection = database.GetCollection<SolarStationInfo>(settings.Value.StationsCollectionName);
    }

    public async Task<List<SolarStationInfo>> GetAllAsync() =>
        await _stationsCollection.Find(_ => true).ToListAsync();

    public async Task<SolarStationInfo?> GetByIdAsync(string id) =>
        await _stationsCollection.Find(x => x.Id == id).FirstOrDefaultAsync();

    public async Task CreateAsync(SolarStationInfo station) =>
        await _stationsCollection.InsertOneAsync(station);
}
