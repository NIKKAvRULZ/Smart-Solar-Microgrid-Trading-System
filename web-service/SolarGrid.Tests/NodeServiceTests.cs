// -----------------------------------------------------------------------------
// File: NodeServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for NodeService
// -----------------------------------------------------------------------------

using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;

namespace SolarGrid.Tests;

public class NodeServiceTests
{
    private readonly Mock<INodeRepository> _mockNodeRepo;
    private readonly Mock<IReservationRepository> _mockResRepo;
    private readonly NodeService _service;

    public NodeServiceTests()
    {
        _mockNodeRepo = new Mock<INodeRepository>();
        _mockResRepo = new Mock<IReservationRepository>();
        _service = new NodeService(_mockNodeRepo.Object, _mockResRepo.Object);
    }

    [Fact]
    public async Task CreateAsync_ValidRequest_SetsAvailableBatterySlotsEqualTotal()
    {
        // Arrange
        var request = new CreateNodeRequest
        {
            Name = "Matara Hub Alpha",
            Latitude = 5.9549,
            Longitude = 80.5550,
            CapacityKWh = 500.0,
            TotalBatterySlots = 10,
            OperatingSchedule = new List<string> { "06:00 - 18:00" }
        };

        _mockNodeRepo.Setup(r => r.CreateAsync(It.IsAny<Node>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal("Matara Hub Alpha", result.Name);
        Assert.Equal(10, result.TotalBatterySlots);
        Assert.Equal(10, result.AvailableBatterySlots);
        Assert.True(result.IsActive);
    }

    [Fact]
    public async Task DeactivateAsync_WhenActiveReservationsExist_ThrowsArgumentException()
    {
        // Arrange
        var nodeId = "node-alpha";
        var existingNode = new Node { Id = nodeId, Name = "Alpha", IsActive = true };

        _mockNodeRepo.Setup(r => r.GetByIdAsync(nodeId)).ReturnsAsync(existingNode);

        var reservations = new List<Reservation>
        {
            new() { Id = "res-1", NodeId = nodeId, Status = ReservationStatus.Approved }
        };
        _mockResRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(reservations);

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.DeactivateAsync(nodeId));
        Assert.Contains("Cannot deactivate node: active reservations exist", ex.Message);
    }

    [Fact]
    public async Task DeactivateAsync_WhenNoActiveReservations_DeactivatesNode()
    {
        // Arrange
        var nodeId = "node-beta";
        var existingNode = new Node { Id = nodeId, Name = "Beta", IsActive = true };

        _mockNodeRepo.Setup(r => r.GetByIdAsync(nodeId)).ReturnsAsync(existingNode);

        var reservations = new List<Reservation>
        {
            new() { Id = "res-1", NodeId = nodeId, Status = ReservationStatus.Completed },
            new() { Id = "res-2", NodeId = nodeId, Status = ReservationStatus.Cancelled }
        };
        _mockResRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(reservations);
        _mockNodeRepo.Setup(r => r.UpdateAsync(nodeId, It.IsAny<Node>())).Returns(Task.CompletedTask);

        // Act
        var success = await _service.DeactivateAsync(nodeId);

        // Assert
        Assert.True(success);
        Assert.False(existingNode.IsActive);
    }
}
