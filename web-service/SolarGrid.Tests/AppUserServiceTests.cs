// -----------------------------------------------------------------------------
// File: AppUserServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for AppUserService
// -----------------------------------------------------------------------------

using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;


namespace SolarGrid.Tests;

public class AppUserServiceTests
{
    private readonly Mock<IAppUserRepository> _mockUserRepo;
    private readonly Mock<IProsumerRepository> _mockProsumerRepo;
    private readonly AppUserService _service;

    public AppUserServiceTests()
    {
        _mockUserRepo = new Mock<IAppUserRepository>();
        _mockProsumerRepo = new Mock<IProsumerRepository>();
        _service = new AppUserService(_mockUserRepo.Object, _mockProsumerRepo.Object);
    }

    [Fact]
    public async Task CreateAsync_UniqueUsername_CreatesActiveUserWithHashedPassword()
    {
        // Arrange
        var request = new CreateUserRequest
        {
            Username = "new_officer",
            Password = "SecurePassword@123",
            FullName = "New Officer",
            Email = "officer@solargrid.com",
            Role = UserRole.Backoffice
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("new_officer")).ReturnsAsync((AppUser?)null);
        _mockUserRepo.Setup(r => r.CreateAsync(It.IsAny<AppUser>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal("new_officer", result.Username);
        Assert.True(result.IsActive);
        Assert.NotEqual("SecurePassword@123", result.PasswordHash);
        _mockUserRepo.Verify(r => r.CreateAsync(It.Is<AppUser>(u => u.Username == "new_officer" && u.IsActive)), Times.Once);
    }

    [Fact]
    public async Task CreateAsync_DuplicateUsername_ReturnsNull()
    {
        // Arrange
        var request = new CreateUserRequest
        {
            Username = "existing_user",
            Password = "Password@123",
            FullName = "Existing",
            Email = "existing@solargrid.com"
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("existing_user"))
                     .ReturnsAsync(new AppUser { Username = "existing_user" });

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.Null(result);
        _mockUserRepo.Verify(r => r.CreateAsync(It.IsAny<AppUser>()), Times.Never);
    }

    [Fact]
    public async Task CreateAsync_WhenRoleIsProsumer_AutoSyncsToProsumersCollection()
    {
        // Arrange
        var request = new CreateUserRequest
        {
            Username = "199512345678",
            Password = "Password@123",
            FullName = "Auto Prosumer",
            Email = "prosumer@test.com",
            Role = UserRole.Prosumer
        };

        _mockUserRepo.Setup(r => r.GetByUsernameAsync("199512345678")).ReturnsAsync((AppUser?)null);
        _mockUserRepo.Setup(r => r.CreateAsync(It.IsAny<AppUser>())).Returns(Task.CompletedTask);
        _mockProsumerRepo.Setup(r => r.GetByNicAsync("199512345678")).ReturnsAsync((Prosumer?)null);
        _mockProsumerRepo.Setup(r => r.CreateAsync(It.IsAny<Prosumer>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.NotNull(result);
        _mockProsumerRepo.Verify(r => r.CreateAsync(It.Is<Prosumer>(p => p.Nic == "199512345678")), Times.Once);
    }

    [Fact]
    public async Task UpdateAsync_ExistingId_UpdatesFields()
    {
        // Arrange
        var userId = "user-123";
        var existing = new AppUser { Id = userId, FullName = "Old Name", Email = "old@test.com", Role = UserRole.GridOperator, IsActive = true };

        _mockUserRepo.Setup(r => r.GetByIdAsync(userId)).ReturnsAsync(existing);
        _mockUserRepo.Setup(r => r.UpdateAsync(userId, It.IsAny<AppUser>())).Returns(Task.CompletedTask);

        var updateReq = new UpdateUserRequest
        {
            FullName = "New Name",
            Email = "new@test.com",
            Role = UserRole.Backoffice,
            IsActive = false
        };

        // Act
        var result = await _service.UpdateAsync(userId, updateReq);

        // Assert
        Assert.NotNull(result);
        Assert.Equal("New Name", result.FullName);
        Assert.Equal("new@test.com", result.Email);
        Assert.Equal(UserRole.Backoffice, result.Role);
        Assert.False(result.IsActive);
    }

    [Fact]
    public async Task DeactivateAsync_ExistingUser_SetsIsActiveFalse()
    {
        // Arrange
        var userId = "user-active";
        var user = new AppUser { Id = userId, IsActive = true };

        _mockUserRepo.Setup(r => r.GetByIdAsync(userId)).ReturnsAsync(user);
        _mockUserRepo.Setup(r => r.UpdateAsync(userId, It.IsAny<AppUser>())).Returns(Task.CompletedTask);

        // Act
        var success = await _service.DeactivateAsync(userId);

        // Assert
        Assert.True(success);
        Assert.False(user.IsActive);
    }
}
