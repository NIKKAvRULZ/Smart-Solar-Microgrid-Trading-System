using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services
{
    public class BookingService
    {
        private readonly IBookingRepository _bookingRepository;

        public BookingService(IBookingRepository bookingRepository)
        {
            _bookingRepository = bookingRepository;
        }

        public async Task<List<Booking>> GetBookingsByUserAsync(string nic) =>
            await _bookingRepository.GetBookingsByUserAsync(nic);

        public async Task<Booking?> CreateBookingAsync(CreateBookingRequest request)
        {
            // Business Rule: Booking slot must be within the next 7 days
            if (request.SlotTime < DateTime.UtcNow || request.SlotTime > DateTime.UtcNow.AddDays(7))
            {
                throw new ArgumentException("Booking slot must be within the next 7 days from today.");
            }

            var booking = new Booking
            {
                StationId = request.StationId,
                UserNic = request.UserNic,
                EnergyAmount = request.EnergyAmount,
                SlotTime = request.SlotTime,
                Status = "Active",
                QrHash = GenerateQrHash($"{request.UserNic}-{request.StationId}-{request.SlotTime:O}")
            };

            await _bookingRepository.CreateBookingAsync(booking);
            return booking;
        }

        public async Task<Booking?> UpdateBookingAsync(string id, UpdateBookingRequest request)
        {
            var booking = await _bookingRepository.GetBookingByIdAsync(id);
            if (booking == null) return null;

            // Business Rule: Can only modify if current time is strictly >= 12 hours prior to the existing SlotTime
            if (DateTime.UtcNow > booking.SlotTime.AddHours(-12))
            {
                throw new ArgumentException("Bookings can only be modified at least 12 hours before the scheduled slot.");
            }

            // Also ensure the new slot is within 7 days
            if (request.NewSlotTime < DateTime.UtcNow || request.NewSlotTime > DateTime.UtcNow.AddDays(7))
            {
                throw new ArgumentException("New booking slot must be within the next 7 days from today.");
            }

            booking.SlotTime = request.NewSlotTime;
            await _bookingRepository.UpdateBookingAsync(id, booking);
            return booking;
        }

        public async Task<bool> CancelBookingAsync(string id)
        {
            var booking = await _bookingRepository.GetBookingByIdAsync(id);
            if (booking == null) return false;

            // Business Rule: Can only cancel if current time is strictly >= 12 hours prior to the existing SlotTime
            if (DateTime.UtcNow > booking.SlotTime.AddHours(-12))
            {
                throw new ArgumentException("Bookings can only be cancelled at least 12 hours before the scheduled slot.");
            }

            booking.Status = "Cancelled";
            await _bookingRepository.UpdateBookingAsync(id, booking);
            return true;
        }

        public async Task<bool> VerifyBookingAsync(VerifyBookingRequest request)
        {
            var booking = await _bookingRepository.GetBookingByIdAsync(request.BookingId);
            if (booking == null || booking.Status != "Active") return false;

            if (booking.QrHash == request.QrHash)
            {
                booking.Status = "Verified";
                await _bookingRepository.UpdateBookingAsync(booking.Id!, booking);
                return true;
            }
            return false;
        }

        private string GenerateQrHash(string input)
        {
            using var sha256 = SHA256.Create();
            var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(input));
            return Convert.ToBase64String(bytes);
        }
    }
}
