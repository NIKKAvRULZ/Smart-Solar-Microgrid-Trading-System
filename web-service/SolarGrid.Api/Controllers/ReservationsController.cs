using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/reservations")]
public class ReservationsController : ControllerBase
{
    private readonly ReservationService _reservationService;

    public ReservationsController(ReservationService reservationService)
    {
        _reservationService = reservationService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() =>
        Ok(await _reservationService.GetAllAsync());

    [HttpGet("prosumer/{nic}")]
    public async Task<IActionResult> GetByProsumer(string nic) =>
        Ok(await _reservationService.GetByProsumerNicAsync(nic));

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateReservationRequest request)
    {
        try
        {
            var reservation = await _reservationService.CreateAsync(request);
            return CreatedAtAction(nameof(GetAll), reservation);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(string id, [FromBody] UpdateReservationRequest request)
    {
        try
        {
            var reservation = await _reservationService.UpdateAsync(id, request);
            if (reservation == null)
                return NotFound(new { message = "Reservation not found." });

            return Ok(reservation);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPatch("{id}/cancel")]
    public async Task<IActionResult> Cancel(string id)
    {
        try
        {
            var success = await _reservationService.CancelAsync(id);
            if (!success)
                return NotFound(new { message = "Reservation not found." });

            return Ok(new { message = "Reservation cancelled successfully." });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPatch("{id}/approve")]
    public async Task<IActionResult> Approve(string id)
    {
        try
        {
            var reservation = await _reservationService.ApproveAsync(id);
            if (reservation == null)
                return NotFound(new { message = "Reservation not found." });

            return Ok(reservation);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPatch("{id}/complete")]
    public async Task<IActionResult> Complete(string id)
    {
        try
        {
            var reservation = await _reservationService.CompleteAsync(id);
            if (reservation == null)
                return NotFound(new { message = "Reservation not found." });

            return Ok(reservation);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}
