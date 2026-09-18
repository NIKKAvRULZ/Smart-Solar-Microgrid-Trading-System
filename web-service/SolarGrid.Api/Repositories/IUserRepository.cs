using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories
{
    public interface IUserRepository
    {
        Task<User?> GetUserByNicAsync(string nic);
        Task CreateUserAsync(User user);
        Task UpdateUserAsync(string nic, User user);
    }
}
