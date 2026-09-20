using MongoDB.Driver;
using SolarGrid.Api.Configuration;
using SolarGrid.Api.Repositories;
using SolarGrid.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// ── Database ──────────────────────────────────────────────────────────────────
builder.Services.Configure<SolarGridDatabaseSettings>(
    builder.Configuration.GetSection("SolarGridDatabase"));

builder.Services.AddSingleton<IMongoClient>(s =>
{
    var settings = builder.Configuration
        .GetSection("SolarGridDatabase")
        .Get<SolarGridDatabaseSettings>();
    return new MongoClient(settings!.ConnectionString);
});

// ── CORS — allow the Vite dev server ─────────────────────────────────────────
builder.Services.AddCors(options =>
{
    options.AddPolicy("FrontendPolicy", policy =>
    {
        policy.WithOrigins("http://localhost:5173")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

// ── Repositories ──────────────────────────────────────────────────────────────
builder.Services.AddScoped<IAppUserRepository, AppUserRepository>();
builder.Services.AddScoped<IProsumerRepository, ProsumerRepository>();
builder.Services.AddScoped<INodeRepository, NodeRepository>();
builder.Services.AddScoped<IReservationRepository, ReservationRepository>();

// ── Services ──────────────────────────────────────────────────────────────────
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<AppUserService>();
builder.Services.AddScoped<ProsumerService>();
builder.Services.AddScoped<NodeService>();
builder.Services.AddScoped<ReservationService>();
builder.Services.AddScoped<DashboardService>();

// ── MVC & Swagger ─────────────────────────────────────────────────────────────
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

// CORS must come before routing/auth middleware
app.UseCors("FrontendPolicy");

app.UseAuthorization();
app.MapControllers();

app.Run();
