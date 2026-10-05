// -----------------------------------------------------------------------------
// File: ProsumerServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for ProsumerService
// -----------------------------------------------------------------------------

using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;

namespace SolarGrid.Tests;

public class ProsumerServiceTests
{
    private readonly Mock<IProsumerRepository> _mockRepo;
    private readonly ProsumerService _service;

    public ProsumerServiceTests()
    {
        _mockRepo = new Mock<IProsumerRepository>();
        _service = new ProsumerService(_mockRepo.Object);
    }

    [Fact]
    public async Task CreateAsync_UniqueNic_CreatesActiveProsumer()
    {
        // Arrange
        var request = new CreateProsumerRequest
        {
            Nic = "200055566677",
            FullName = "Amal Silva",
            Email = "amal@gmail.com",
            Phone = "0771234567",
            Address = "Galle, Sri Lanka",
            Password = "ProsumerPassword@123"
        };

        _mockRepo.Setup(r => r.GetByNicAsync("200055566677")).ReturnsAsync((Prosumer?)null);
        _mockRepo.Setup(r => r.CreateAsync(It.IsAny<Prosumer>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal("200055566677", result.Nic);
        Assert.True(result.IsActive);
        Assert.NotEqual("ProsumerPassword@123", result.PasswordHash);
        _mockRepo.Verify(r => r.CreateAsync(It.Is<Prosumer>(p => p.Nic == "200055566677")), Times.Once);
    }

    [Fact]
    public async Task CreateAsync_DuplicateNic_ReturnsNull()
    {
        // Arrange
        var request = new CreateProsumerRequest
        {
            Nic = "200055566677",
            FullName = "Amal Silva",
            Password = "Pass"
        };

        _mockRepo.Setup(r => r.GetByNicAsync("200055566677"))
                 .ReturnsAsync(new Prosumer { Nic = "200055566677" });

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.Null(result);
        _mockRepo.Verify(r => r.CreateAsync(It.IsAny<Prosumer>()), Times.Never);
    }

    [Fact]
    public async Task DeactivateAsync_ExistingNic_SetsIsActiveFalse()
    {
        // Arrange
        var prosumer = new Prosumer { Nic = "200055566677", IsActive = true };
        _mockRepo.Setup(r => r.GetByNicAsync("200055566677")).ReturnsAsync(prosumer);
        _mockRepo.Setup(r => r.UpdateAsync("200055566677", It.IsAny<Prosumer>())).Returns(Task.CompletedTask);

        // Act
        var success = await _service.DeactivateAsync("200055566677");

        // Assert
        Assert.True(success);
        Assert.False(prosumer.IsActive);
    }

    [Fact]
    public async Task ReactivateAsync_ExistingNic_SetsIsActiveTrue()
    {
        // Arrange
        var prosumer = new Prosumer { Nic = "200055566677", IsActive = false };
        _mockRepo.Setup(r => r.GetByNicAsync("200055566677")).ReturnsAsync(prosumer);
        _mockRepo.Setup(r => r.UpdateAsync("200055566677", It.IsAny<Prosumer>())).Returns(Task.CompletedTask);

        // Act
        var success = await _service.ReactivateAsync("200055566677");

        // Assert
        Assert.True(success);
        Assert.True(prosumer.IsActive);
    }
}
