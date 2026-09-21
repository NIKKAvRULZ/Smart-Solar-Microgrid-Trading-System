namespace SolarGrid.Api.Models;

public class CreateNodeRequest
{
    public string Name { get; set; } = null!;
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public double CapacityKWh { get; set; }
    public int TotalBatterySlots { get; set; }
    public List<string> OperatingSchedule { get; set; } = new();
}

public class UpdateNodeRequest
{
    public string Name { get; set; } = null!;
    public double CapacityKWh { get; set; }
    public int TotalBatterySlots { get; set; }
    public int AvailableBatterySlots { get; set; }
    public List<string> OperatingSchedule { get; set; } = new();
}
