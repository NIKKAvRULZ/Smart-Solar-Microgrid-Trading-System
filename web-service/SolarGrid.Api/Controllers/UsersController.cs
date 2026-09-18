using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly UserService _userService;

        public UsersController(UserService userService)
        {
            _userService = userService;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request)
        {
            var user = await _userService.RegisterAsync(request);
            if (user == null)
            {
                return BadRequest(new { message = "NIC is already registered." });
            }
            return CreatedAtAction(nameof(Register), new { id = user.Id }, user);
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var response = await _userService.LoginAsync(request);
            if (response == null)
            {
                return Unauthorized(new { message = "Invalid credentials or account deactivated." });
            }
            return Ok(response);
        }

        [HttpPut("{nic}/deactivate")]
        public async Task<IActionResult> Deactivate(string nic)
        {
            var success = await _userService.DeactivateAsync(nic);
            if (!success)
            {
                return NotFound(new { message = "User not found." });
            }
            return Ok(new { message = "Account successfully deactivated." });
        }
    }
}
