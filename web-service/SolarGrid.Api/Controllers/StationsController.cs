using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/v1/[controller]")]
public class StationsController : ControllerBase
{
    private readonly StationService _stationService;

    public StationsController(StationService stationService)
    {
        _stationService = stationService;
    }

    [HttpGet]
    public async Task<ActionResult<List<SolarStationInfo>>> Get()
    {
        var stations = await _stationService.GetStationsAsync();
        return Ok(stations);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<SolarStationInfo>> Get(string id)
    {
        var station = await _stationService.GetStationAsync(id);

        if (station == null)
        {
            return NotFound();
        }

        return Ok(station);
    }

    [HttpPost]
    public async Task<IActionResult> Post(SolarStationInfo newStation)
    {
        await _stationService.CreateStationAsync(newStation);
        return CreatedAtAction(nameof(Get), new { id = newStation.Id }, newStation);
    }
}
