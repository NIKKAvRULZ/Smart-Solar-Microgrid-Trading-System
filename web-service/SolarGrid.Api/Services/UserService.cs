using System.Security.Cryptography;
using System.Text;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services
{
    public class UserService
    {
        private readonly IUserRepository _userRepository;

        public UserService(IUserRepository userRepository)
        {
            _userRepository = userRepository;
        }

        public async Task<User?> RegisterAsync(RegisterRequest request)
        {
            var existingUser = await _userRepository.GetUserByNicAsync(request.Nic);
            if (existingUser != null)
                return null; // NIC already registered

            var user = new User
            {
                Nic = request.Nic,
                Name = request.Name,
                PasswordHash = HashPassword(request.Password),
                Role = request.Role,
                IsActive = true
            };

            await _userRepository.CreateUserAsync(user);
            return user;
        }

        public async Task<LoginResponse?> LoginAsync(LoginRequest request)
        {
            var user = await _userRepository.GetUserByNicAsync(request.Nic);
            
            if (user == null || !user.IsActive)
                return null;

            if (user.PasswordHash != HashPassword(request.Password))
                return null;

            // In a real production app, generate a JWT token here.
            // For this assignment, we generate a simple session token string.
            var token = Guid.NewGuid().ToString();

            return new LoginResponse
            {
                Token = token,
                Role = user.Role,
                Name = user.Name
            };
        }

        public async Task<bool> DeactivateAsync(string nic)
        {
            var user = await _userRepository.GetUserByNicAsync(nic);
            if (user == null) return false;

            user.IsActive = false;
            await _userRepository.UpdateUserAsync(nic, user);
            return true;
        }

        private string HashPassword(string password)
        {
            using var sha256 = SHA256.Create();
            var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
            return Convert.ToBase64String(bytes);
        }
    }
}
