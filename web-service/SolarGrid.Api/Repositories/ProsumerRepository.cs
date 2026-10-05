// -----------------------------------------------------------------------------
// File: ProsumerRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for ProsumerRepository
// -----------------------------------------------------------------------------

using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public class ProsumerRepository : IProsumerRepository
{
    private readonly IMongoCollection<Prosumer> _collection;

    public ProsumerRepository(IOptions<SolarGridDatabaseSettings> settings, IMongoClient mongoClient)
    {
        var db = mongoClient.GetDatabase(settings.Value.DatabaseName);
        _collection = db.GetCollection<Prosumer>(settings.Value.ProsumersCollectionName);
    }

    // Inline comment: Expression body method execution
    public async Task<List<Prosumer>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    // Inline comment: Expression body method execution
    public async Task<Prosumer?> GetByNicAsync(string nic) =>
        await _collection.Find(x => x.Nic == nic).FirstOrDefaultAsync();

    // Inline comment: Expression body method execution
    public async Task CreateAsync(Prosumer prosumer) =>
        await _collection.InsertOneAsync(prosumer);

    // Inline comment: Expression body method execution
    public async Task UpdateAsync(string nic, Prosumer prosumer) =>
        await _collection.ReplaceOneAsync(x => x.Nic == nic, prosumer);
}
