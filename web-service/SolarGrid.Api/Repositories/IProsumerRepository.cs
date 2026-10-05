// -----------------------------------------------------------------------------
// File: IProsumerRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for IProsumerRepository
// -----------------------------------------------------------------------------

using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface IProsumerRepository
{
    Task<List<Prosumer>> GetAllAsync();
    Task<Prosumer?> GetByNicAsync(string nic);
    Task CreateAsync(Prosumer prosumer);
    Task UpdateAsync(string nic, Prosumer prosumer);
}
