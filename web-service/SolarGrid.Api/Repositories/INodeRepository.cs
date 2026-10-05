// -----------------------------------------------------------------------------
// File: INodeRepository.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for INodeRepository
// -----------------------------------------------------------------------------

using SolarGrid.Api.Models;

namespace SolarGrid.Api.Repositories;

public interface INodeRepository
{
    Task<List<Node>> GetAllAsync();
    Task<Node?> GetByIdAsync(string id);
    Task CreateAsync(Node node);
    Task UpdateAsync(string id, Node node);
}
