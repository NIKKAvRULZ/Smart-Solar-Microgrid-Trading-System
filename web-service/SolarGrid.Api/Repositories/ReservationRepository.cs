using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public class ReservationRepository : IReservationRepository
{
    private readonly IMongoCollection<Reservation> _collection;

    public ReservationRepository(IOptions<SolarGridDatabaseSettings> settings, IMongoClient mongoClient)
    {
        var db = mongoClient.GetDatabase(settings.Value.DatabaseName);
        _collection = db.GetCollection<Reservation>(settings.Value.ReservationsCollectionName);
    }

    public async Task<List<Reservation>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    public async Task<List<Reservation>> GetByProsumerNicAsync(string nic) =>
        await _collection.Find(x => x.ProsumerNic == nic).ToListAsync();

    public async Task<Reservation?> GetByIdAsync(string id) =>
        await _collection.Find(x => x.Id == id).FirstOrDefaultAsync();

    public async Task CreateAsync(Reservation reservation) =>
        await _collection.InsertOneAsync(reservation);

    public async Task UpdateAsync(string id, Reservation reservation) =>
        await _collection.ReplaceOneAsync(x => x.Id == id, reservation);
}
