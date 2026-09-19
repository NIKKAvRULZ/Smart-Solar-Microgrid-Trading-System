using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

/// <summary>
/// Solar energy prosumer — a property owner with a solar array who trades
/// energy via the mobile app. Managed (registered/deactivated) by Backoffice.
/// </summary>
public class Prosumer
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    [JsonIgnore]
    public string? InternalId { get; set; }

    /// <summary>National Identity Card number — primary business key.</summary>
    [JsonPropertyName("nic")]
    public string Nic { get; set; } = null!;

    [JsonPropertyName("fullName")]
    public string FullName { get; set; } = null!;

    [JsonPropertyName("email")]
    public string Email { get; set; } = null!;

    [JsonPropertyName("phone")]
    public string Phone { get; set; } = null!;

    [JsonPropertyName("address")]
    public string Address { get; set; } = null!;

    /// <summary>SHA-256 hash of the mobile-app password — never sent to API consumers.</summary>
    [JsonIgnore]
    public string PasswordHash { get; set; } = null!;

    [JsonPropertyName("isActive")]
    public bool IsActive { get; set; } = true;
}
