namespace SolarGrid.Api.Models
{
    public class CreateBookingRequest
    {
        public string StationId { get; set; } = null!;
        public string UserNic { get; set; } = null!;
        public double EnergyAmount { get; set; }
        public DateTime SlotTime { get; set; }
    }

    public class UpdateBookingRequest
    {
        public DateTime NewSlotTime { get; set; }
    }

    public class VerifyBookingRequest
    {
        public string BookingId { get; set; } = null!;
        public string QrHash { get; set; } = null!;
    }
}
