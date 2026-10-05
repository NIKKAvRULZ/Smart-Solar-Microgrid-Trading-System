// -----------------------------------------------------------------------------
// File: AuthServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for AuthService
// -----------------------------------------------------------------------------

using System.Security.Cryptography;
using System.Text;
using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;

namespace SolarGrid.Tests;

public class AuthServiceTests
{
    private readonly Mock<IAppUserRepository> _mockUserRepo;
    private readonly Mock<IProsumerRepository> _mockProsumerRepo;
    private readonly AuthService _service;

    public AuthServiceTests()
    {
        _mockUserRepo = new Mock<IAppUserRepository>();
        _mockProsumerRepo = new Mock<IProsumerRepository>();
        _service = new AuthService(_mockUserRepo.Object, _mockProsumerRepo.Object);
    }

    private static string Hash(string password)
    {
        using var sha256 = SHA256.Create();
        var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(bytes);
    }

    [Fact]
    public async Task LoginAsync_ValidAppUser_ReturnsLoginResponseWithToken()
    {
        // Arrange
        var user = new AppUser
        {
            Id = "usr-1",
            Username = "admin",
            FullName = "Admin User",
            PasswordHash = Hash("Admin@123"),
            Role = UserRole.Backoffice,
            IsActive = true
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("admin")).ReturnsAsync(user);

        // Act
        var result = await _service.LoginAsync(new LoginRequest { Username = "admin", Password = "Admin@123" });

        // Assert
        Assert.NotNull(result);
        Assert.Equal("admin", result.Username);
        Assert.Equal("Admin User", result.FullName);
        Assert.Equal(UserRole.Backoffice, result.Role);
        Assert.False(string.IsNullOrEmpty(result.Token));
    }

    [Fact]
    public async Task LoginAsync_AppUserWrongPassword_ReturnsNull()
    {
        // Arrange
        var user = new AppUser
        {
            Username = "admin",
            PasswordHash = Hash("CorrectPassword"),
            IsActive = true
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("admin")).ReturnsAsync(user);

        // Act
        var result = await _service.LoginAsync(new LoginRequest { Username = "admin", Password = "WrongPassword" });

        // Assert
        Assert.Null(result);
    }

    [Fact]
    public async Task LoginAsync_InactiveAppUser_ReturnsNull()
    {
        // Arrange
        var user = new AppUser
        {
            Username = "deactivated_operator",
            PasswordHash = Hash("Pass123"),
            IsActive = false
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("deactivated_operator")).ReturnsAsync(user);

        // Act
        var result = await _service.LoginAsync(new LoginRequest { Username = "deactivated_operator", Password = "Pass123" });

        // Assert
        Assert.Null(result);
    }

    [Fact]
    public async Task LoginAsync_ValidProsumerFallback_ReturnsProsumerLoginResponse()
    {
        // Arrange
        var prosumer = new Prosumer
        {
            Nic = "199912345678",
            FullName = "Jane Prosumer",
            PasswordHash = Hash("Prosumer@123"),
            IsActive = true
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("199912345678")).ReturnsAsync((AppUser?)null);
        _mockProsumerRepo.Setup(r => r.GetByNicAsync("199912345678")).ReturnsAsync(prosumer);

        // Act
        var result = await _service.LoginAsync(new LoginRequest { Username = "199912345678", Password = "Prosumer@123" });

        // Assert
        Assert.NotNull(result);
        Assert.Equal("199912345678", result.Username);
        Assert.Equal("Jane Prosumer", result.FullName);
        Assert.Equal(UserRole.Prosumer, result.Role);
        Assert.False(string.IsNullOrEmpty(result.Token));
    }

    [Fact]
    public async Task LoginAsync_UnknownUser_ReturnsNull()
    {
        // Arrange
        _mockUserRepo.Setup(r => r.GetByUsernameAsync("nonexistent")).ReturnsAsync((AppUser?)null);
        _mockProsumerRepo.Setup(r => r.GetByNicAsync("nonexistent")).ReturnsAsync((Prosumer?)null);

        // Act
        var result = await _service.LoginAsync(new LoginRequest { Username = "nonexistent", Password = "AnyPassword" });

        // Assert
        Assert.Null(result);
    }
}
