using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

/// <summary>
/// A microgrid solar hub — a physical location where prosumers can deposit
/// or draw energy. Replaces the old SolarStationInfo model.
/// </summary>
public class Node
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    [JsonPropertyName("id")]
    public string? Id { get; set; }

    [JsonPropertyName("name")]
    public string Name { get; set; } = null!;

    [JsonPropertyName("latitude")]
    public double Latitude { get; set; }

    [JsonPropertyName("longitude")]
    public double Longitude { get; set; }

    /// <summary>Total storage capacity of the node in kilowatt-hours.</summary>
    [JsonPropertyName("capacityKWh")]
    public double CapacityKWh { get; set; }

    /// <summary>Total number of battery storage slots at this node.</summary>
    [JsonPropertyName("totalBatterySlots")]
    public int TotalBatterySlots { get; set; }

    /// <summary>Slots currently free (not reserved). Updated by Grid Operators.</summary>
    [JsonPropertyName("availableBatterySlots")]
    public int AvailableBatterySlots { get; set; }

    /// <summary>Human-readable schedule strings, e.g. "Mon-Fri 06:00-20:00".</summary>
    [JsonPropertyName("operatingSchedule")]
    public List<string> OperatingSchedule { get; set; } = new();

    [JsonPropertyName("isActive")]
    public bool IsActive { get; set; } = true;
}
