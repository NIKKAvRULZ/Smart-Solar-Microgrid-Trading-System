namespace SolarGrid.Api.Models;

public class CreateReservationRequest
{
    public string ProsumerNic { get; set; } = null!;
    public string NodeId { get; set; } = null!;
    public DateTime ScheduledDateTime { get; set; }
    public int DurationMinutes { get; set; }
}

public class UpdateReservationRequest
{
    public DateTime ScheduledDateTime { get; set; }
    public int DurationMinutes { get; set; }
}
