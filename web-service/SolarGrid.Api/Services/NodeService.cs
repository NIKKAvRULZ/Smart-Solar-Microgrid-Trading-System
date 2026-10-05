// -----------------------------------------------------------------------------
// File: NodeService.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for NodeService
// -----------------------------------------------------------------------------

using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Manages microgrid node (solar hub) records.
/// Backoffice creates nodes; Grid Operators can update battery slot counts.
/// </summary>
public class NodeService
{
    private readonly INodeRepository _nodeRepository;
    private readonly IReservationRepository _reservationRepository;

    public NodeService(INodeRepository nodeRepository, IReservationRepository reservationRepository)
    {
        _nodeRepository = nodeRepository;
        _reservationRepository = reservationRepository;
    }

    // Inline comment: Expression body method execution
    public async Task<List<Node>> GetAllAsync() =>
        await _nodeRepository.GetAllAsync();

    // Inline comment: Expression body method execution
    public async Task<Node?> GetByIdAsync(string id) =>
        await _nodeRepository.GetByIdAsync(id);

    public async Task<Node> CreateAsync(CreateNodeRequest request)
    {
        // Inline comment: Method execution begins here.
        var node = new Node
        {
            Name                  = request.Name,
            Latitude              = request.Latitude,
            Longitude             = request.Longitude,
            CapacityKWh           = request.CapacityKWh,
            TotalBatterySlots     = request.TotalBatterySlots,
            // All slots are available when first registered
            AvailableBatterySlots = request.TotalBatterySlots,
            OperatingSchedule     = request.OperatingSchedule,
            IsActive              = true
        };

        await _nodeRepository.CreateAsync(node);
        return node;
    }

    public async Task<Node?> UpdateAsync(string id, UpdateNodeRequest request)
    {
        // Inline comment: Method execution begins here.
        var node = await _nodeRepository.GetByIdAsync(id);
        if (node == null) return null;

        node.Name                  = request.Name;
        node.CapacityKWh           = request.CapacityKWh;
        node.TotalBatterySlots     = request.TotalBatterySlots;
        node.AvailableBatterySlots = request.AvailableBatterySlots;
        node.OperatingSchedule     = request.OperatingSchedule;

        await _nodeRepository.UpdateAsync(id, node);
        return node;
    }

    public async Task<bool> DeactivateAsync(string id)
    {
        // Inline comment: Method execution begins here.
        var node = await _nodeRepository.GetByIdAsync(id);
        if (node == null) return false;

        // Block deactivation if active energy reservations exist
        var allReservations = await _reservationRepository.GetAllAsync();
        bool hasActiveReservations = allReservations.Any(r => r.NodeId == id && (r.Status == ReservationStatus.Pending || r.Status == ReservationStatus.Approved));
        
        if (hasActiveReservations)
            throw new ArgumentException("Cannot deactivate node: active reservations exist.");

        node.IsActive = false;
        await _nodeRepository.UpdateAsync(id, node);
        return true;
    }
}
