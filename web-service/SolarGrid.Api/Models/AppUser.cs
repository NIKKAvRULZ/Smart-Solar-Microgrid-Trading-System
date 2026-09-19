using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

/// <summary>
/// Web application user — Backoffice officers and Grid Operators who log in
/// to this React dashboard. Distinct from Prosumers (mobile-app users).
/// </summary>
public class AppUser
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    [JsonPropertyName("id")]
    public string? Id { get; set; }

    [JsonPropertyName("username")]
    public string Username { get; set; } = null!;

    /// <summary>SHA-256 password hash — never serialised to the API response.</summary>
    [JsonIgnore]
    public string PasswordHash { get; set; } = null!;

    [JsonPropertyName("fullName")]
    public string FullName { get; set; } = null!;

    [JsonPropertyName("email")]
    public string Email { get; set; } = null!;

    /// <summary>Backoffice | GridOperator</summary>
    [JsonPropertyName("role")]
    public string Role { get; set; } = "GridOperator";

    [JsonPropertyName("isActive")]
    public bool IsActive { get; set; } = true;
}
