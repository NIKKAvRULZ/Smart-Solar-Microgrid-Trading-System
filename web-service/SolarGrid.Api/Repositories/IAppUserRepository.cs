// -----------------------------------------------------------------------------
// File: IAppUserRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for IAppUserRepository
// -----------------------------------------------------------------------------

using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface IAppUserRepository
{
    Task<List<AppUser>> GetAllAsync();
    Task<AppUser?> GetByIdAsync(string id);
    Task<AppUser?> GetByUsernameAsync(string username);
    Task CreateAsync(AppUser user);
    Task UpdateAsync(string id, AppUser user);
}
