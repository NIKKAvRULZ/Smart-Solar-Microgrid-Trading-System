using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public class NodeRepository : INodeRepository
{
    private readonly IMongoCollection<Node> _collection;

    public NodeRepository(IOptions<SolarGridDatabaseSettings> settings, IMongoClient mongoClient)
    {
        var db = mongoClient.GetDatabase(settings.Value.DatabaseName);
        _collection = db.GetCollection<Node>(settings.Value.NodesCollectionName);
    }

    public async Task<List<Node>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    public async Task<Node?> GetByIdAsync(string id) =>
        await _collection.Find(x => x.Id == id).FirstOrDefaultAsync();

    public async Task CreateAsync(Node node) =>
        await _collection.InsertOneAsync(node);

    public async Task UpdateAsync(string id, Node node) =>
        await _collection.ReplaceOneAsync(x => x.Id == id, node);
}
