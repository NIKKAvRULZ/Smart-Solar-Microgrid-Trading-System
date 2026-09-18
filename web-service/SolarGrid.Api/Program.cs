var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.Configure<SolarGrid.Api.Configuration.SolarGridDatabaseSettings>(
    builder.Configuration.GetSection("SolarGridDatabase"));

builder.Services.AddSingleton<MongoDB.Driver.IMongoClient>(s =>
{
    var settings = builder.Configuration.GetSection("SolarGridDatabase").Get<SolarGrid.Api.Configuration.SolarGridDatabaseSettings>();
    return new MongoDB.Driver.MongoClient(settings!.ConnectionString);
});

// Register Repositories and Services
builder.Services.AddScoped<SolarGrid.Api.Repositories.IStationRepository, SolarGrid.Api.Repositories.StationRepository>();
builder.Services.AddScoped<SolarGrid.Api.Services.StationService>();
builder.Services.AddScoped<SolarGrid.Api.Repositories.IUserRepository, SolarGrid.Api.Repositories.UserRepository>();
builder.Services.AddScoped<SolarGrid.Api.Services.UserService>();
builder.Services.AddScoped<SolarGrid.Api.Repositories.IBookingRepository, SolarGrid.Api.Repositories.BookingRepository>();
builder.Services.AddScoped<SolarGrid.Api.Services.BookingService>();

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline.
app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
