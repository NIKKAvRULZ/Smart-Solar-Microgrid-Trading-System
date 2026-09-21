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

    public async Task<List<AppUser>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    public async Task<AppUser?> GetByIdAsync(string id) =>
        await _collection.Find(x => x.Id == id).FirstOrDefaultAsync();

    public async Task<AppUser?> GetByUsernameAsync(string username) =>
        await _collection.Find(x => x.Username == username).FirstOrDefaultAsync();

    public async Task CreateAsync(AppUser user) =>
        await _collection.InsertOneAsync(user);

    public async Task UpdateAsync(string id, AppUser user) =>
        await _collection.ReplaceOneAsync(x => x.Id == id, user);
}
