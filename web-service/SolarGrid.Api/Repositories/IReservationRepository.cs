using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface IReservationRepository
{
    Task<List<Reservation>> GetAllAsync();
    Task<List<Reservation>> GetByProsumerNicAsync(string nic);
    Task<Reservation?> GetByIdAsync(string id);
    Task CreateAsync(Reservation reservation);
    Task UpdateAsync(string id, Reservation reservation);
}
