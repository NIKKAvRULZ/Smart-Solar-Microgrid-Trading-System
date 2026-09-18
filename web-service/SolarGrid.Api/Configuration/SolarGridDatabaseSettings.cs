namespace SolarGrid.Api.Configuration;

public class SolarGridDatabaseSettings
{
    public string ConnectionString { get; set; } = null!;
    public string DatabaseName { get; set; } = null!;
    public string StationsCollectionName { get; set; } = null!;
    public string UsersCollectionName { get; set; } = null!;
    public string BookingsCollectionName { get; set; } = null!;
}
