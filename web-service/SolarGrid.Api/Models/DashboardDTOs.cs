using System.Text.Json.Serialization;

namespace SolarGrid.Api.Models;

/// <summary>
/// Aggregated analytics for the Backoffice dashboard. All values are computed
/// from the live Nodes, Prosumers and Reservations collections — nothing is
/// hard-coded. Energy figures are derived from completed reservations, using
/// each node's real per-slot storage capacity.
/// </summary>
public class DashboardAnalytics
{
    [JsonPropertyName("generatedAt")]
    public DateTime GeneratedAt { get; set; }

    [JsonPropertyName("period")]
    public string Period { get; set; } = "all";

    [JsonPropertyName("nodes")]
    public DashboardNodeStats Nodes { get; set; } = new();

    [JsonPropertyName("prosumers")]
    public DashboardProsumerStats Prosumers { get; set; } = new();

    [JsonPropertyName("reservations")]
    public DashboardReservationStats Reservations { get; set; } = new();

    [JsonPropertyName("energy")]
    public DashboardEnergyStats Energy { get; set; } = new();
}

public class DashboardNodeStats
{
    [JsonPropertyName("total")]
    public int Total { get; set; }

    [JsonPropertyName("active")]
    public int Active { get; set; }

    [JsonPropertyName("offline")]
    public int Offline { get; set; }

    [JsonPropertyName("capacityKWh")]
    public double CapacityKWh { get; set; }

    /// <summary>Total storage capacity held by online nodes, in kWh.</summary>
    [JsonPropertyName("activeCapacityKWh")]
    public double ActiveCapacityKWh { get; set; }

    [JsonPropertyName("totalBatterySlots")]
    public int TotalBatterySlots { get; set; }

    [JsonPropertyName("availableBatterySlots")]
    public int AvailableBatterySlots { get; set; }

    [JsonPropertyName("availabilityPct")]
    public double AvailabilityPct { get; set; }
}

public class DashboardProsumerStats
{
    [JsonPropertyName("total")]
    public int Total { get; set; }

    [JsonPropertyName("active")]
    public int Active { get; set; }

    [JsonPropertyName("activePct")]
    public double ActivePct { get; set; }
}

public class DashboardReservationStats
{
    [JsonPropertyName("total")]
    public int Total { get; set; }

    [JsonPropertyName("pending")]
    public int Pending { get; set; }

    [JsonPropertyName("approved")]
    public int Approved { get; set; }

    [JsonPropertyName("completed")]
    public int Completed { get; set; }

    [JsonPropertyName("cancelled")]
    public int Cancelled { get; set; }

    [JsonPropertyName("latest")]
    public List<DashboardLatestReservation> Latest { get; set; } = new();
}

public class DashboardLatestReservation
{
    [JsonPropertyName("id")]
    public string Id { get; set; } = null!;

    [JsonPropertyName("scheduledDateTime")]
    public DateTime ScheduledDateTime { get; set; }

    [JsonPropertyName("status")]
    public string Status { get; set; } = "Pending";
}

public class DashboardEnergyStats
{
    /// <summary>Energy from completed reservations scheduled today, in kWh.</summary>
    [JsonPropertyName("todayKWh")]
    public double TodayKWh { get; set; }

    /// <summary>Energy from completed reservations scheduled yesterday, in kWh.</summary>
    [JsonPropertyName("yesterdayKWh")]
    public double YesterdayKWh { get; set; }

    [JsonPropertyName("todayDeltaPct")]
    public double TodayDeltaPct { get; set; }

    /// <summary>Daily energy totals for the last seven days (oldest first).</summary>
    [JsonPropertyName("last7Days")]
    public List<DashboardDailyEnergy> Last7Days { get; set; } = new();
}

public class DashboardDailyEnergy
{
    /// <summary>UTC day bucket in yyyy-MM-dd format.</summary>
    [JsonPropertyName("date")]
    public string Date { get; set; } = null!;

    [JsonPropertyName("kwh")]
    public double Kwh { get; set; }
}