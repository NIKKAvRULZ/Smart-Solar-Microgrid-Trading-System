// -----------------------------------------------------------------------------
// File: DashboardServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for DashboardService
// -----------------------------------------------------------------------------

using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;

namespace SolarGrid.Tests;

public class DashboardServiceTests
{
    private readonly Mock<INodeRepository> _mockNodeRepo;
    private readonly Mock<IProsumerRepository> _mockProsumerRepo;
    private readonly Mock<IReservationRepository> _mockResRepo;
    private readonly DashboardService _service;

    public DashboardServiceTests()
    {
        _mockNodeRepo = new Mock<INodeRepository>();
        _mockProsumerRepo = new Mock<IProsumerRepository>();
        _mockResRepo = new Mock<IReservationRepository>();
        _service = new DashboardService(_mockNodeRepo.Object, _mockProsumerRepo.Object, _mockResRepo.Object);
    }

    [Fact]
    public async Task GetAnalyticsAsync_EmptyDataset_ReturnsSafeZerosWithoutExceptions()
    {
        // Arrange
        _mockNodeRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(new List<Node>());
        _mockProsumerRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(new List<Prosumer>());
        _mockResRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(new List<Reservation>());

        // Act
        var result = await _service.GetAnalyticsAsync("all");

        // Assert
        Assert.NotNull(result);
        Assert.Equal(0, result.Nodes.Total);
        Assert.Equal(0, result.Nodes.CapacityKWh);
        Assert.Equal(0, result.Prosumers.Total);
        Assert.Equal(0, result.Reservations.Total);
        Assert.Equal(0, result.Energy.TodayKWh);
    }

    [Fact]
    public async Task GetAnalyticsAsync_PopulatedData_CalculatesAggregatesCorrectly()
    {
        // Arrange
        var nodes = new List<Node>
        {
            new() { Id = "n1", Name = "Hub 1", CapacityKWh = 100, TotalBatterySlots = 10, AvailableBatterySlots = 4, IsActive = true },
            new() { Id = "n2", Name = "Hub 2", CapacityKWh = 200, TotalBatterySlots = 20, AvailableBatterySlots = 10, IsActive = false }
        };

        var prosumers = new List<Prosumer>
        {
            new() { Nic = "nic-1", IsActive = true },
            new() { Nic = "nic-2", IsActive = true },
            new() { Nic = "nic-3", IsActive = false }
        };

        var reservations = new List<Reservation>
        {
            new() { Id = "r1", EnergyAmount = 25.5, Status = ReservationStatus.Completed, ScheduledDateTime = DateTime.UtcNow },
            new() { Id = "r2", EnergyAmount = 15.0, Status = ReservationStatus.Pending, ScheduledDateTime = DateTime.UtcNow },
            new() { Id = "r3", EnergyAmount = 10.0, Status = ReservationStatus.Cancelled, ScheduledDateTime = DateTime.UtcNow }
        };

        _mockNodeRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(nodes);
        _mockProsumerRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(prosumers);
        _mockResRepo.Setup(r => r.GetAllAsync()).ReturnsAsync(reservations);

        // Act
        var result = await _service.GetAnalyticsAsync("all");

        // Assert
        Assert.NotNull(result);
        Assert.Equal(2, result.Nodes.Total);
        Assert.Equal(1, result.Nodes.Active);
        Assert.Equal(1, result.Nodes.Offline);
        Assert.Equal(300, result.Nodes.CapacityKWh);

        Assert.Equal(3, result.Prosumers.Total);
        Assert.Equal(2, result.Prosumers.Active);

        Assert.Equal(3, result.Reservations.Total);
        Assert.Equal(1, result.Reservations.Completed);
        Assert.Equal(1, result.Reservations.Pending);
        Assert.Equal(1, result.Reservations.Cancelled);
    }
}
