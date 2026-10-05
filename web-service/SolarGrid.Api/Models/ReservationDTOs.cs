// -----------------------------------------------------------------------------
// File: ReservationDTOs.cs
// Author: Perera W. A. N. I.
// Purpose: System implementation for ReservationDTOs
// -----------------------------------------------------------------------------

namespace SolarGrid.Api.Models;

public class CreateReservationRequest
{
    public string ProsumerNic { get; set; } = null!;
    public string NodeId { get; set; } = null!;
    public DateTime ScheduledDateTime { get; set; }
    public int DurationMinutes { get; set; }
    public double EnergyAmount { get; set; } // Added Field
}

public class UpdateReservationRequest
{
    public DateTime ScheduledDateTime { get; set; }
    public int DurationMinutes { get; set; }
    public double EnergyAmount { get; set; } // Added Field
}