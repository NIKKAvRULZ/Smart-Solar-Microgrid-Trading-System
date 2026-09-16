namespace SolarGrid.Api.Configuration;

public class SolarGridDatabaseSettings
{
    public string ConnectionString { get; set; } = null!;
    public string DatabaseName { get; set; } = null!;
    public string StationsCollectionName { get; set; } = null!;
}
