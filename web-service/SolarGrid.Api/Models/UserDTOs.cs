namespace SolarGrid.Api.Models
{
    public class RegisterRequest
    {
        public string Nic { get; set; } = null!;
        public string Name { get; set; } = null!;
        public string Password { get; set; } = null!;
        public string Role { get; set; } = "Prosumer";
    }

    public class LoginRequest
    {
        public string Nic { get; set; } = null!;
        public string Password { get; set; } = null!;
    }

    public class LoginResponse
    {
        public string Token { get; set; } = null!;
        public string Role { get; set; } = null!;
        public string Name { get; set; } = null!;
    }
}
