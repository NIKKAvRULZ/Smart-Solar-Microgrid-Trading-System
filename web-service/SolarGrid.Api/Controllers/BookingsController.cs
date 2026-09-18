using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class BookingsController : ControllerBase
    {
        private readonly BookingService _bookingService;

        public BookingsController(BookingService bookingService)
        {
            _bookingService = bookingService;
        }

        [HttpGet("{nic}")]
        public async Task<IActionResult> GetBookings(string nic)
        {
            var bookings = await _bookingService.GetBookingsByUserAsync(nic);
            return Ok(bookings);
        }

        [HttpPost]
        public async Task<IActionResult> CreateBooking([FromBody] CreateBookingRequest request)
        {
            try
            {
                var booking = await _bookingService.CreateBookingAsync(request);
                return CreatedAtAction(nameof(GetBookings), new { nic = booking!.UserNic }, booking);
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateBooking(string id, [FromBody] UpdateBookingRequest request)
        {
            try
            {
                var booking = await _bookingService.UpdateBookingAsync(id, request);
                if (booking == null) return NotFound(new { message = "Booking not found." });
                return Ok(booking);
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> CancelBooking(string id)
        {
            try
            {
                var success = await _bookingService.CancelBookingAsync(id);
                if (!success) return NotFound(new { message = "Booking not found." });
                return Ok(new { message = "Booking cancelled successfully." });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("verify")]
        public async Task<IActionResult> VerifyBooking([FromBody] VerifyBookingRequest request)
        {
            var success = await _bookingService.VerifyBookingAsync(request);
            if (!success)
            {
                return BadRequest(new { message = "Verification failed. Invalid QR Hash or booking is not active." });
            }
            return Ok(new { message = "Booking verified successfully by Operator." });
        }
    }
}
