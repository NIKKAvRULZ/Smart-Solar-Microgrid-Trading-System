namespace SolarGrid.Api.Configuration;

public class SolarGridDatabaseSettings
{
    public string ConnectionString { get; set; } = null!;
    public string DatabaseName { get; set; } = null!;
    public string AppUsersCollectionName { get; set; } = null!;
    public string ProsumersCollectionName { get; set; } = null!;
    public string NodesCollectionName { get; set; } = null!;
    public string ReservationsCollectionName { get; set; } = null!;
}
