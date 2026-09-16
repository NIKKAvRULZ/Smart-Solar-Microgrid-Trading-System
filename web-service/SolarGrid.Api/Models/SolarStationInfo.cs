using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

public class SolarStationInfo
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("name")]
    [JsonPropertyName("name")]
    public string Name { get; set; } = null!;

    [BsonElement("address")]
    [JsonPropertyName("address")]
    public string Address { get; set; } = null!;

    [BsonElement("latitude")]
    [JsonPropertyName("latitude")]
    public double Latitude { get; set; }

    [BsonElement("longitude")]
    [JsonPropertyName("longitude")]
    public double Longitude { get; set; }

    [BsonElement("powerCapacityKw")]
    [JsonPropertyName("powerCapacityKw")]
    public double PowerCapacityKw { get; set; }

    [BsonElement("status")]
    [JsonPropertyName("status")]
    public string Status { get; set; } = "ACTIVE";
}
