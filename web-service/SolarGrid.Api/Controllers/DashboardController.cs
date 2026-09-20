using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/dashboard")]
public class DashboardController : ControllerBase
{
    private readonly DashboardService _dashboardService;

    public DashboardController(DashboardService dashboardService)
    {
        _dashboardService = dashboardService;
    }

    /// <summary>
    /// Live analytics for the Backoffice dashboard. All values are computed
    /// from the current database state.
    /// </summary>
    /// <param name="period">all | today | 7d — scopes the reservation counts.</param>
    [HttpGet]
    public async Task<IActionResult> Get([FromQuery] string period = "all")
    {
        var normalized = period.ToLowerInvariant() switch
        {
            "today" => "today",
            "7d" or "7" or "week" or "thisweek" => "week",
            "month" or "thismonth" => "month",
            _ => "all",
        };
        return Ok(await _dashboardService.GetAnalyticsAsync(normalized));
    }
}