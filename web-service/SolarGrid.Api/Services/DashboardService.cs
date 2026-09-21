using SolarGrid.Api.Models;
using SolarGrid.Api.Repositories;

namespace SolarGrid.Api.Services;

/// <summary>
/// Server-side aggregation for the Backoffice dashboard. Every figure is
/// derived from live repository data (Nodes, Prosumers, Reservations) so the
/// dashboard always reflects the current database state and never hard-codes
/// values. Safe division is used throughout so empty datasets return 0.
/// </summary>
public class DashboardService
{
    private const int LatestCount = 5;
    private const int ChartDays = 7;

    private readonly INodeRepository _nodeRepository;
    private readonly IProsumerRepository _prosumerRepository;
    private readonly IReservationRepository _reservationRepository;

    public DashboardService(
        INodeRepository nodeRepository,
        IProsumerRepository prosumerRepository,
        IReservationRepository reservationRepository)
    {
        _nodeRepository = nodeRepository;
        _prosumerRepository = prosumerRepository;
        _reservationRepository = reservationRepository;
    }

    public async Task<DashboardAnalytics> GetAnalyticsAsync(string period)
    {
        var nodes = await _nodeRepository.GetAllAsync();
        var prosumers = await _prosumerRepository.GetAllAsync();
        var reservations = await _reservationRepository.GetAllAsync();

        var analytics = new DashboardAnalytics
        {
            GeneratedAt = DateTime.UtcNow,
            Period = period,
            Nodes = BuildNodeStats(nodes),
            Prosumers = BuildProsumerStats(prosumers),
            Reservations = BuildReservationStats(reservations, period),
            Energy = BuildEnergyStats(reservations, nodes),
        };

        return analytics;
    }

    private static DashboardNodeStats BuildNodeStats(List<Node> nodes)
    {
        double capacity = 0;
        double activeCapacity = 0;
        var totalSlots = 0;
        var availSlots = 0;
        var active = 0;

        foreach (var node in nodes)
        {
            capacity += node.CapacityKWh;
            totalSlots += node.TotalBatterySlots;
            availSlots += node.AvailableBatterySlots;

            if (node.IsActive)
            {
                active++;
                activeCapacity += node.CapacityKWh;
            }
        }

        return new DashboardNodeStats
        {
            Total = nodes.Count,
            Active = active,
            Offline = nodes.Count - active,
            CapacityKWh = Round(capacity),
            ActiveCapacityKWh = Round(activeCapacity),
            TotalBatterySlots = totalSlots,
            AvailableBatterySlots = availSlots,
            AvailabilityPct = Round(SafePercent(availSlots, totalSlots)),
        };
    }

    private static DashboardProsumerStats BuildProsumerStats(List<Prosumer> prosumers)
    {
        var active = prosumers.Count(p => p.IsActive);
        return new DashboardProsumerStats
        {
            Total = prosumers.Count,
            Active = active,
            ActivePct = Round(SafePercent(active, prosumers.Count)),
        };
    }

    private static DashboardReservationStats BuildReservationStats(List<Reservation> reservations, string period)
    {
        var start = PeriodStart(period, DateTime.UtcNow);
        var inPeriod = reservations.Where(r => r.ScheduledDateTime >= start).ToList();

        var stats = new DashboardReservationStats
        {
            Total = inPeriod.Count,
            Pending = inPeriod.Count(r => r.Status == "Pending"),
            Approved = inPeriod.Count(r => r.Status == "Approved"),
            Completed = inPeriod.Count(r => r.Status == "Completed"),
            Cancelled = inPeriod.Count(r => r.Status == "Cancelled"),
            Latest = reservations
                .OrderByDescending(r => r.ScheduledDateTime)
                .Take(LatestCount)
                .Select(r => new DashboardLatestReservation
                {
                    Id = r.Id ?? string.Empty,
                    ScheduledDateTime = r.ScheduledDateTime,
                    Status = r.Status,
                })
                .ToList(),
        };

        return stats;
    }

    private static DashboardEnergyStats BuildEnergyStats(List<Reservation> reservations, List<Node> nodes)
    {
        var today = DateTime.UtcNow.Date;
        var yesterday = today.AddDays(-1);

        var buckets = new Dictionary<DateTime, double>();
        for (var i = ChartDays - 1; i >= 0; i--)
            buckets[today.AddDays(-i)] = 0;

        var todayKwh = 0d;
        var yesterdayKwh = 0d;

        foreach (var r in reservations)
        {
            if (r.Status != "Completed")
                continue;

            var kwh = SlotCapacityKwh(nodes, r.NodeId);
            var day = r.ScheduledDateTime.Date;

            if (day == today)
                todayKwh += kwh;

            if (day == yesterday)
                yesterdayKwh += kwh;

            if (buckets.ContainsKey(day))
                buckets[day] += kwh;
        }

        // Safe difference: when yesterday had no energy and today has some,
        // report a 100% increase; otherwise report the true percentage or 0.
        double delta;
        if (todayKwh > 0 && yesterdayKwh <= 0)
            delta = 100;
        else if (yesterdayKwh <= 0)
            delta = 0;
        else
            delta = (todayKwh - yesterdayKwh) / yesterdayKwh * 100;

        return new DashboardEnergyStats
        {
            TodayKWh = Round(todayKwh),
            YesterdayKWh = Round(yesterdayKwh),
            TodayDeltaPct = Round(delta),
            Last7Days = buckets
                .OrderBy(b => b.Key)
                .Select(b => new DashboardDailyEnergy
                {
                    Date = b.Key.ToString("yyyy-MM-dd"),
                    Kwh = Round(b.Value),
                })
                .ToList(),
        };
    }

    /// <summary>
    /// Energy attributed to one completed reservation at a node — the node's
    /// total storage capacity divided by its real battery-slot count.
    /// </summary>
    private static double SlotCapacityKwh(List<Node> nodes, string nodeId)
    {
        var node = nodes.FirstOrDefault(n => n.Id == nodeId);
        if (node == null || node.TotalBatterySlots <= 0)
            return 0;
        return node.CapacityKWh / node.TotalBatterySlots;
    }

    private static DateTime PeriodStart(string period, DateTime now) =>
        period switch
        {
            "today" => now.Date,
            "7d" => now.Date.AddDays(-(ChartDays - 1)),
            "week" => StartOfWeek(now, DayOfWeek.Monday),
            "month" => new DateTime(now.Year, now.Month, 1, 0, 0, 0, DateTimeKind.Utc),
            _ => DateTime.MinValue.ToUniversalTime(),
        };

    /// <summary>
    /// Start of the calendar week that contains <paramref name="date"/>,
    /// aligned to the requested first day of week (e.g. Monday).
    /// </summary>
    private static DateTime StartOfWeek(DateTime date, DayOfWeek firstDay)
    {
        var daysSinceFirstDay = ((int)date.DayOfWeek - (int)firstDay + 7) % 7;
        return date.Date.AddDays(-daysSinceFirstDay).ToUniversalTime();
    }

    private static double SafePercent(int value, int total) =>
        total > 0 ? value * 100.0 / total : 0;

    private static double Round(double value) => Math.Round(value, 2);
}