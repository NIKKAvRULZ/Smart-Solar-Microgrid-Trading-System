using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories
{
    public class BookingRepository : IBookingRepository
    {
        private readonly IMongoCollection<Booking> _bookingsCollection;

        public BookingRepository(IOptions<SolarGridDatabaseSettings> databaseSettings, MongoClient mongoClient)
        {
            var mongoDatabase = mongoClient.GetDatabase(databaseSettings.Value.DatabaseName);
            _bookingsCollection = mongoDatabase.GetCollection<Booking>(databaseSettings.Value.BookingsCollectionName);
        }

        public async Task<List<Booking>> GetBookingsByUserAsync(string nic) =>
            await _bookingsCollection.Find(x => x.UserNic == nic).ToListAsync();

        public async Task<Booking?> GetBookingByIdAsync(string id) =>
            await _bookingsCollection.Find(x => x.Id == id).FirstOrDefaultAsync();

        public async Task CreateBookingAsync(Booking booking) =>
            await _bookingsCollection.InsertOneAsync(booking);

        public async Task UpdateBookingAsync(string id, Booking booking) =>
            await _bookingsCollection.ReplaceOneAsync(x => x.Id == id, booking);

        public async Task DeleteBookingAsync(string id) =>
            await _bookingsCollection.DeleteOneAsync(x => x.Id == id);
    }
}
