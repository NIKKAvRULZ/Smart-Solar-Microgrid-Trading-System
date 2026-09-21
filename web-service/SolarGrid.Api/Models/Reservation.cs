using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

/// <summary>
/// An energy slot reservation made by a prosumer at a microgrid node.
/// Status lifecycle: Pending → Approved (QR issued) → Completed | Cancelled.
/// </summary>
[BsonIgnoreExtraElements]
public class Reservation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    [JsonPropertyName("id")]
    public string? Id { get; set; }

    [JsonPropertyName("prosumerNic")]
    public string ProsumerNic { get; set; } = null!;

    [JsonPropertyName("nodeId")]
    public string NodeId { get; set; } = null!;

    [JsonPropertyName("scheduledDateTime")]
    public DateTime ScheduledDateTime { get; set; }

    [JsonPropertyName("durationMinutes")]
    public int DurationMinutes { get; set; }

    // --- NEW ENERGY AMOUNT FIELD ---
    [JsonPropertyName("energyAmount")]
    public double EnergyAmount { get; set; }

    /// <summary>Pending | Approved | Completed | Cancelled</summary>
    [JsonPropertyName("status")]
    public string Status { get; set; } = "Pending";

    /// <summary>Short QR dispatch code generated when a Grid Operator approves the booking.</summary>
    [JsonPropertyName("qrCode")]
    public string? QrCode { get; set; }

    [JsonPropertyName("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [JsonPropertyName("updatedAt")]
    public DateTime? UpdatedAt { get; set; }
}