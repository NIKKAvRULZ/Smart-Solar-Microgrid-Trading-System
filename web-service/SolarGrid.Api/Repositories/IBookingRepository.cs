using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories
{
    public interface IBookingRepository
    {
        Task<List<Booking>> GetBookingsByUserAsync(string nic);
        Task<Booking?> GetBookingByIdAsync(string id);
        Task CreateBookingAsync(Booking booking);
        Task UpdateBookingAsync(string id, Booking booking);
        Task DeleteBookingAsync(string id);
    }
}
