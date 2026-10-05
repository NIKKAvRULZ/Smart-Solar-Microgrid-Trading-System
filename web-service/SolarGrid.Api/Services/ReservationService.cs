// -----------------------------------------------------------------------------
// File: ReservationService.cs
// Author: Perera W. A. N. I.
// Purpose: System implementation for ReservationService
// -----------------------------------------------------------------------------

using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Manages energy slot reservations with the full business lifecycle:
/// Pending → Approved (QR issued & node resources deducted) → Completed | Cancelled (resources refunded).
///
/// Business rules preserved from original BookingService:
///  - Must be scheduled within the next 7 days
///  - Modification/cancellation requires at least 12 hours' notice
///  - Battery slot and energy capacity deduction on node upon approval
/// </summary>
public class ReservationService
{
    private readonly IReservationRepository _reservationRepository;
    private readonly INodeRepository _nodeRepository;

    public ReservationService(
        IReservationRepository reservationRepository,
        INodeRepository nodeRepository)
    {
        _reservationRepository = reservationRepository;
        _nodeRepository = nodeRepository;
    }

    // Inline comment: Expression body method execution
    public async Task<List<Reservation>> GetAllAsync() =>
        await _reservationRepository.GetAllAsync();

    // Inline comment: Expression body method execution
    public async Task<List<Reservation>> GetByProsumerNicAsync(string nic) =>
        await _reservationRepository.GetByProsumerNicAsync(nic);

    public async Task<Reservation> CreateAsync(CreateReservationRequest request)
    {
        // Inline comment: Method execution begins here.
        if (request.ScheduledDateTime < DateTime.UtcNow ||
            request.ScheduledDateTime > DateTime.UtcNow.AddDays(7))
        {
            throw new ArgumentException("Reservation must be scheduled within the next 7 days.");
        }

        // Validate station node existence and available slots
        var node = await _nodeRepository.GetByIdAsync(request.NodeId);
        if (node == null || !node.IsActive)
        {
            throw new ArgumentException("Selected microgrid station does not exist or is inactive.");
        }

        if (node.AvailableBatterySlots <= 0)
        {
            throw new ArgumentException("This station has no available battery slots.");
        }

        if (request.EnergyAmount <= 0)
        {
            throw new ArgumentException("Requested energy amount must be greater than zero.");
        }

        if (request.EnergyAmount > node.CapacityKWh)
        {
            throw new ArgumentException($"Requested energy ({request.EnergyAmount} kWh) exceeds available capacity ({node.CapacityKWh} kWh).");
        }

        var reservation = new Reservation
        {
            ProsumerNic = request.ProsumerNic,
            NodeId = request.NodeId,
            ScheduledDateTime = request.ScheduledDateTime,
            DurationMinutes = request.DurationMinutes,
            EnergyAmount = request.EnergyAmount,
            Status = ReservationStatus.Pending,
            CreatedAt = DateTime.UtcNow
        };

        await _reservationRepository.CreateAsync(reservation);
        return reservation;
    }

    public async Task<Reservation?> UpdateAsync(string id, UpdateReservationRequest request)
    {
        // Inline comment: Method execution begins here.
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status is ReservationStatus.Completed or ReservationStatus.Cancelled)
            throw new ArgumentException("Cannot modify a completed or cancelled reservation.");

        if (DateTime.UtcNow > reservation.ScheduledDateTime.AddHours(-12))
            throw new ArgumentException("Reservations can only be modified at least 12 hours before the scheduled time.");

        if (request.ScheduledDateTime < DateTime.UtcNow ||
            request.ScheduledDateTime > DateTime.UtcNow.AddDays(7))
        {
            throw new ArgumentException("New scheduled time must be within the next 7 days.");
        }

        reservation.ScheduledDateTime = request.ScheduledDateTime;
        reservation.DurationMinutes = request.DurationMinutes;
        reservation.EnergyAmount = request.EnergyAmount;
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    public async Task<bool> CancelAsync(string id)
    {
        // Inline comment: Method execution begins here.
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return false;

        if (reservation.Status is ReservationStatus.Completed or ReservationStatus.Cancelled)
            throw new ArgumentException("This reservation is already completed or cancelled.");

        if (DateTime.UtcNow > reservation.ScheduledDateTime.AddHours(-12))
            throw new ArgumentException("Reservations can only be cancelled at least 12 hours before the scheduled time.");

        // If the reservation was Approved, refund the battery slot and energy back to the station node
        if (reservation.Status == ReservationStatus.Approved)
        {
            var node = await _nodeRepository.GetByIdAsync(reservation.NodeId);
            if (node != null)
            {
                node.AvailableBatterySlots = Math.Min(node.TotalBatterySlots, node.AvailableBatterySlots + 1);
                node.CapacityKWh += reservation.EnergyAmount;
                await _nodeRepository.UpdateAsync(node.Id!, node);
            }
        }

        reservation.Status = ReservationStatus.Cancelled;
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return true;
    }

    /// <summary>
    /// Approving a reservation generates a short QR dispatch code and deducts
    /// one battery storage slot and requested energy amount from the microgrid node.
    /// </summary>
    public async Task<Reservation?> ApproveAsync(string id)
    {
        // Inline comment: Method execution begins here.
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status != ReservationStatus.Pending)
            throw new ArgumentException("Only Pending reservations can be approved.");

        // Fetch station node and validate remaining availability
        var node = await _nodeRepository.GetByIdAsync(reservation.NodeId);
        if (node == null || !node.IsActive)
            throw new ArgumentException("Microgrid station does not exist or is inactive.");

        if (node.AvailableBatterySlots <= 0)
            throw new ArgumentException("No available battery storage slots remaining at this station.");

        // Deduct 1 slot and energy amount on the node
        node.AvailableBatterySlots -= 1;
        if (reservation.EnergyAmount > 0)
        {
            node.CapacityKWh = Math.Max(0, node.CapacityKWh - reservation.EnergyAmount);
        }
        await _nodeRepository.UpdateAsync(node.Id!, node);

        // Update reservation to Approved with QR code
        reservation.Status = ReservationStatus.Approved;
        reservation.QrCode = GenerateQrCode($"{reservation.ProsumerNic}-{reservation.NodeId}-{reservation.ScheduledDateTime:O}");
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    public async Task<Reservation?> CompleteAsync(string id)
    {
        // Inline comment: Method execution begins here.
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status != ReservationStatus.Approved)
            throw new ArgumentException("Only Approved reservations can be completed.");

        reservation.Status = ReservationStatus.Completed;
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    /// <summary>Returns a short uppercase hex string suitable for display as a QR code value.</summary>
    private static string GenerateQrCode(string input)
    {
        // Inline comment: Method execution begins here.
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(input));
        return Convert.ToHexString(bytes)[..12];
    }
}