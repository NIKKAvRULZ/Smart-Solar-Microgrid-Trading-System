// -----------------------------------------------------------------------------
// File: AppUserRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for AppUserRepository
// -----------------------------------------------------------------------------

using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public class AppUserRepository : IAppUserRepository
{
    private readonly IMongoCollection<AppUser> _collection;

    public AppUserRepository(IOptions<SolarGridDatabaseSettings> settings, IMongoClient mongoClient)
    {
        var db = mongoClient.GetDatabase(settings.Value.DatabaseName);
        _collection = db.GetCollection<AppUser>(settings.Value.AppUsersCollectionName);
    }

    // Inline comment: Expression body method execution
    public async Task<List<AppUser>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    // Inline comment: Expression body method execution
    public async Task<AppUser?> GetByIdAsync(string id) =>
        await _collection.Find(x => x.Id == id).FirstOrDefaultAsync();

    // Inline comment: Expression body method execution
    public async Task<AppUser?> GetByUsernameAsync(string username) =>
        await _collection.Find(x => x.Username == username).FirstOrDefaultAsync();

    // Inline comment: Expression body method execution
    public async Task CreateAsync(AppUser user) =>
        await _collection.InsertOneAsync(user);

    // Inline comment: Expression body method execution
    public async Task UpdateAsync(string id, AppUser user) =>
        await _collection.ReplaceOneAsync(x => x.Id == id, user);
}
