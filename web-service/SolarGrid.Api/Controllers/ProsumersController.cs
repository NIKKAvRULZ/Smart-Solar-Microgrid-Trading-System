using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/prosumers")]
public class ProsumersController : ControllerBase
{
    private readonly ProsumerService _prosumerService;

    public ProsumersController(ProsumerService prosumerService)
    {
        _prosumerService = prosumerService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() =>
        Ok(await _prosumerService.GetAllAsync());

    [HttpGet("{nic}")]
    public async Task<IActionResult> GetByNic(string nic)
    {
        var prosumer = await _prosumerService.GetByNicAsync(nic);
        if (prosumer == null)
            return NotFound(new { message = "Prosumer not found." });

        return Ok(prosumer);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateProsumerRequest request)
    {
        var prosumer = await _prosumerService.CreateAsync(request);
        if (prosumer == null)
            return BadRequest(new { message = "A prosumer with this NIC is already registered." });

        return CreatedAtAction(nameof(GetAll), prosumer);
    }

    [HttpPut("{nic}")]
    public async Task<IActionResult> Update(string nic, [FromBody] UpdateProsumerRequest request)
    {
        var prosumer = await _prosumerService.UpdateAsync(nic, request);
        if (prosumer == null)
            return NotFound(new { message = "Prosumer not found." });

        return Ok(prosumer);
    }

    [HttpPatch("{nic}/deactivate")]
    public async Task<IActionResult> Deactivate(string nic)
    {
        var success = await _prosumerService.DeactivateAsync(nic);
        if (!success)
            return NotFound(new { message = "Prosumer not found." });

        return Ok(new { message = "Prosumer deactivated successfully." });
    }

    [HttpPatch("{nic}/reactivate")]
    public async Task<IActionResult> Reactivate(string nic)
    {
        var success = await _prosumerService.ReactivateAsync(nic);
        if (!success)
            return NotFound(new { message = "Prosumer not found." });

        return Ok(new { message = "Prosumer reactivated successfully." });
    }
}
