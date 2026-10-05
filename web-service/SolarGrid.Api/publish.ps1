Write-Host "Taking app offline..." -ForegroundColor Yellow
$publishDir = ".\bin\Release\net8.0\publish"
if (!(Test-Path $publishDir)) {
    New-Item -ItemType Directory -Force -Path $publishDir | Out-Null
}
Set-Content -Path "$publishDir\app_offline.htm" -Value "Offline for update"

Write-Host "Publishing new DLLs..." -ForegroundColor Cyan
dotnet publish SolarGrid.Api.csproj -c Release -o $publishDir

Write-Host "Bringing app online..." -ForegroundColor Green
Remove-Item -Path "$publishDir\app_offline.htm" -Force

Write-Host "Done! Refresh your browser at http://localhost:8080/swagger" -ForegroundColor Green
