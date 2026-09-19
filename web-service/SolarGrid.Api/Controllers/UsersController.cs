using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/users")]
public class UsersController : ControllerBase
{
    private readonly AppUserService _userService;

    public UsersController(AppUserService userService)
    {
        _userService = userService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() =>
        Ok(await _userService.GetAllAsync());

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateUserRequest request)
    {
        var user = await _userService.CreateAsync(request);
        if (user == null)
            return BadRequest(new { message = "Username is already taken." });

        return CreatedAtAction(nameof(GetAll), user);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(string id, [FromBody] UpdateUserRequest request)
    {
        var user = await _userService.UpdateAsync(id, request);
        if (user == null)
            return NotFound(new { message = "User not found." });

        return Ok(user);
    }

    [HttpPatch("{id}/deactivate")]
    public async Task<IActionResult> Deactivate(string id)
    {
        var success = await _userService.DeactivateAsync(id);
        if (!success)
            return NotFound(new { message = "User not found." });

        return Ok(new { message = "User deactivated successfully." });
    }
}
