// -----------------------------------------------------------------------------
// File: ReservationServiceTests.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: Unit tests for ReservationService
// -----------------------------------------------------------------------------

using Moq;
using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;
using Xunit;

namespace SolarGrid.Tests;

public class ReservationServiceTests
{
    private readonly Mock<IReservationRepository> _mockRepo;
    private readonly Mock<INodeRepository> _mockNodeRepo;
    private readonly ReservationService _service;

    public ReservationServiceTests()
    {
        _mockRepo = new Mock<IReservationRepository>();
        _mockNodeRepo = new Mock<INodeRepository>();
        _service = new ReservationService(_mockRepo.Object, _mockNodeRepo.Object);

        _mockNodeRepo.Setup(n => n.GetByIdAsync(It.IsAny<string>()))
                     .ReturnsAsync(new Node
                     {
                         Id = "node-101",
                         Name = "Test Hub",
                         CapacityKWh = 500,
                         TotalBatterySlots = 10,
                         AvailableBatterySlots = 10,
                         IsActive = true
                     });
    }

    [Fact]
    public async Task CreateAsync_ValidWithin7Days_CreatesPendingReservation()
    {
        // Arrange
        var request = new CreateReservationRequest
        {
            ProsumerNic = "200012345678",
            NodeId = "node-101",
            ScheduledDateTime = DateTime.UtcNow.AddDays(2),
            DurationMinutes = 60,
            EnergyAmount = 12.5
        };

        _mockRepo.Setup(r => r.CreateAsync(It.IsAny<Reservation>()))
                 .Returns(Task.CompletedTask);

        // Act
        var result = await _service.CreateAsync(request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal(ReservationStatus.Pending, result.Status);
        Assert.Equal("200012345678", result.ProsumerNic);
        Assert.Equal("node-101", result.NodeId);
        _mockRepo.Verify(r => r.CreateAsync(It.Is<Reservation>(res => res.Status == ReservationStatus.Pending)), Times.Once);
    }

    [Fact]
    public async Task CreateAsync_ScheduledInPast_ThrowsArgumentException()
    {
        // Arrange
        var request = new CreateReservationRequest
        {
            ProsumerNic = "200012345678",
            NodeId = "node-101",
            ScheduledDateTime = DateTime.UtcNow.AddMinutes(-30),
            DurationMinutes = 60,
            EnergyAmount = 10.0
        };

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.CreateAsync(request));
        Assert.Contains("within the next 7 days", ex.Message);
    }

    [Fact]
    public async Task CreateAsync_ScheduledMoreThan7DaysAhead_ThrowsArgumentException()
    {
        // Arrange
        var request = new CreateReservationRequest
        {
            ProsumerNic = "200012345678",
            NodeId = "node-101",
            ScheduledDateTime = DateTime.UtcNow.AddDays(8),
            DurationMinutes = 60,
            EnergyAmount = 10.0
        };

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.CreateAsync(request));
        Assert.Contains("within the next 7 days", ex.Message);
    }

    [Fact]
    public async Task CancelAsync_WithNoticeGreaterThan12Hours_CancelsSuccessfully()
    {
        // Arrange
        var reservationId = "res-1";
        var existing = new Reservation
        {
            Id = reservationId,
            ScheduledDateTime = DateTime.UtcNow.AddHours(24),
            Status = ReservationStatus.Pending
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);
        _mockRepo.Setup(r => r.UpdateAsync(reservationId, It.IsAny<Reservation>())).Returns(Task.CompletedTask);

        // Act
        var success = await _service.CancelAsync(reservationId);

        // Assert
        Assert.True(success);
        Assert.Equal(ReservationStatus.Cancelled, existing.Status);
        _mockRepo.Verify(r => r.UpdateAsync(reservationId, It.Is<Reservation>(r => r.Status == ReservationStatus.Cancelled)), Times.Once);
    }

    [Fact]
    public async Task CancelAsync_NoticeLessThan12Hours_ThrowsArgumentException()
    {
        // Arrange
        var reservationId = "res-2";
        var existing = new Reservation
        {
            Id = reservationId,
            ScheduledDateTime = DateTime.UtcNow.AddHours(5), // < 12 hours notice
            Status = ReservationStatus.Pending
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.CancelAsync(reservationId));
        Assert.Contains("at least 12 hours before", ex.Message);
    }

    [Fact]
    public async Task CancelAsync_AlreadyCompleted_ThrowsArgumentException()
    {
        // Arrange
        var reservationId = "res-3";
        var existing = new Reservation
        {
            Id = reservationId,
            ScheduledDateTime = DateTime.UtcNow.AddHours(24),
            Status = ReservationStatus.Completed
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.CancelAsync(reservationId));
        Assert.Contains("already completed or cancelled", ex.Message);
    }

    [Fact]
    public async Task ApproveAsync_WhenPending_GeneratesQrCodeAndSetsApproved()
    {
        // Arrange
        var reservationId = "res-4";
        var existing = new Reservation
        {
            Id = reservationId,
            ProsumerNic = "200012345678",
            NodeId = "node-101",
            ScheduledDateTime = DateTime.UtcNow.AddDays(1),
            Status = ReservationStatus.Pending
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);
        _mockRepo.Setup(r => r.UpdateAsync(reservationId, It.IsAny<Reservation>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.ApproveAsync(reservationId);

        // Assert
        Assert.NotNull(result);
        Assert.Equal(ReservationStatus.Approved, result.Status);
        Assert.False(string.IsNullOrEmpty(result.QrCode));
        Assert.Equal(12, result.QrCode.Length); // 12-char hex QR code
    }

    [Fact]
    public async Task ApproveAsync_WhenNotPending_ThrowsArgumentException()
    {
        // Arrange
        var reservationId = "res-5";
        var existing = new Reservation
        {
            Id = reservationId,
            Status = ReservationStatus.Approved
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);

        // Act & Assert
        var ex = await Assert.ThrowsAsync<ArgumentException>(() => _service.ApproveAsync(reservationId));
        Assert.Contains("Only Pending reservations can be approved", ex.Message);
    }

    [Fact]
    public async Task CompleteAsync_WhenApproved_SetsStatusCompleted()
    {
        // Arrange
        var reservationId = "res-6";
        var existing = new Reservation
        {
            Id = reservationId,
            Status = ReservationStatus.Approved,
            QrCode = "ABCD1234EF56"
        };

        _mockRepo.Setup(r => r.GetByIdAsync(reservationId)).ReturnsAsync(existing);
        _mockRepo.Setup(r => r.UpdateAsync(reservationId, It.IsAny<Reservation>())).Returns(Task.CompletedTask);

        // Act
        var result = await _service.CompleteAsync(reservationId);

        // Assert
        Assert.NotNull(result);
        Assert.Equal(ReservationStatus.Completed, result.Status);
    }
}
