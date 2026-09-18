using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly IMongoCollection<User> _usersCollection;

        public UserRepository(IOptions<SolarGridDatabaseSettings> databaseSettings, MongoClient mongoClient)
        {
            var mongoDatabase = mongoClient.GetDatabase(databaseSettings.Value.DatabaseName);
            _usersCollection = mongoDatabase.GetCollection<User>(databaseSettings.Value.UsersCollectionName);
        }

        public async Task<User?> GetUserByNicAsync(string nic) =>
            await _usersCollection.Find(x => x.Nic == nic).FirstOrDefaultAsync();

        public async Task CreateUserAsync(User user) =>
            await _usersCollection.InsertOneAsync(user);

        public async Task UpdateUserAsync(string nic, User user) =>
            await _usersCollection.ReplaceOneAsync(x => x.Nic == nic, user);
    }
}
