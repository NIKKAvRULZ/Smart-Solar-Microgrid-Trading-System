using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Manages energy slot reservations with the full business lifecycle:
/// Pending → Approved (QR issued) → Completed  |  Cancelled.
///
/// Business rules preserved from original BookingService:
///  - Must be scheduled within the next 7 days
///  - Modification/cancellation requires at least 12 hours' notice
/// </summary>
public class ReservationService
{
    private readonly IReservationRepository _reservationRepository;

    public ReservationService(IReservationRepository reservationRepository)
    {
        _reservationRepository = reservationRepository;
    }

    public async Task<List<Reservation>> GetAllAsync() =>
        await _reservationRepository.GetAllAsync();

    public async Task<List<Reservation>> GetByProsumerNicAsync(string nic) =>
        await _reservationRepository.GetByProsumerNicAsync(nic);

    public async Task<Reservation> CreateAsync(CreateReservationRequest request)
    {
        if (request.ScheduledDateTime < DateTime.UtcNow ||
            request.ScheduledDateTime > DateTime.UtcNow.AddDays(7))
        {
            throw new ArgumentException("Reservation must be scheduled within the next 7 days.");
        }

        var reservation = new Reservation
        {
            ProsumerNic = request.ProsumerNic,
            NodeId = request.NodeId,
            ScheduledDateTime = request.ScheduledDateTime,
            DurationMinutes = request.DurationMinutes,
            EnergyAmount = request.EnergyAmount, // <-- ADDED FIELD
            Status = "Pending",
            CreatedAt = DateTime.UtcNow
        };

        await _reservationRepository.CreateAsync(reservation);
        return reservation;
    }

    public async Task<Reservation?> UpdateAsync(string id, UpdateReservationRequest request)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status is "Completed" or "Cancelled")
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
        reservation.EnergyAmount = request.EnergyAmount; // <-- ADDED FIELD
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    public async Task<bool> CancelAsync(string id)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return false;

        if (reservation.Status is "Completed" or "Cancelled")
            throw new ArgumentException("This reservation is already completed or cancelled.");

        if (DateTime.UtcNow > reservation.ScheduledDateTime.AddHours(-12))
            throw new ArgumentException("Reservations can only be cancelled at least 12 hours before the scheduled time.");

        reservation.Status = "Cancelled";
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return true;
    }

    /// <summary>
    /// Approving a reservation generates a short QR dispatch code so the prosumer
    /// can prove identity at the node terminal.
    /// </summary>
    public async Task<Reservation?> ApproveAsync(string id)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status != "Pending")
            throw new ArgumentException("Only Pending reservations can be approved.");

        reservation.Status = "Approved";
        reservation.QrCode = GenerateQrCode($"{reservation.ProsumerNic}-{reservation.NodeId}-{reservation.ScheduledDateTime:O}");
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    public async Task<Reservation?> CompleteAsync(string id)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;

        if (reservation.Status != "Approved")
            throw new ArgumentException("Only Approved reservations can be completed.");

        reservation.Status = "Completed";
        reservation.UpdatedAt = DateTime.UtcNow;

        await _reservationRepository.UpdateAsync(id, reservation);
        return reservation;
    }

    /// <summary>Returns a short uppercase hex string suitable for display as a QR code value.</summary>
    private static string GenerateQrCode(string input)
    {
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(input));
        return Convert.ToHexString(bytes)[..12];
    }
}