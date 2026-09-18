using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolarGrid.Api.Models
{
    public class Booking
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        public string StationId { get; set; } = null!;

        public string UserNic { get; set; } = null!;

        public double EnergyAmount { get; set; }

        public DateTime SlotTime { get; set; }

        public string Status { get; set; } = "Active"; // Active, Cancelled, Verified

        public string QrHash { get; set; } = null!;
    }
}
