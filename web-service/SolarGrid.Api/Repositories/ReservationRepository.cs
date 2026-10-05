// -----------------------------------------------------------------------------
// File: ReservationRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for ReservationRepository
// -----------------------------------------------------------------------------

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

    // Inline comment: Expression body method execution
    public async Task<List<Reservation>> GetAllAsync() =>
        await _collection.Find(_ => true).ToListAsync();

    // Inline comment: Expression body method execution
    public async Task<List<Reservation>> GetByProsumerNicAsync(string nic) =>
        await _collection.Find(x => x.ProsumerNic == nic).ToListAsync();

    // Inline comment: Expression body method execution
    public async Task<Reservation?> GetByIdAsync(string id) =>
        await _collection.Find(x => x.Id == id).FirstOrDefaultAsync();

    // Inline comment: Expression body method execution
    public async Task CreateAsync(Reservation reservation) =>
        await _collection.InsertOneAsync(reservation);

    // Inline comment: Expression body method execution
    public async Task UpdateAsync(string id, Reservation reservation) =>
        await _collection.ReplaceOneAsync(x => x.Id == id, reservation);
}
