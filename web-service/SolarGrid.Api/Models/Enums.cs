// -----------------------------------------------------------------------------
// File: Enums.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for Enums
// -----------------------------------------------------------------------------

namespace SolarGrid.Api.Models;

public enum UserRole
{
    Backoffice,
    GridOperator,
    Prosumer
}

public enum ReservationStatus
{
    Pending,
    Approved,
    Completed,
    Cancelled
}
