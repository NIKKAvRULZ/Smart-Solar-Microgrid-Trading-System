Write-Host "Taking app offline..." -ForegroundColor Yellow
Set-Content -Path ".\bin\Release\net8.0\publish\app_offline.htm" -Value "Offline for update"

Write-Host "Publishing new DLLs..." -ForegroundColor Cyan
dotnet publish -c Release -o ./bin/Release/net8.0/publish

Write-Host "Bringing app online..." -ForegroundColor Green
Remove-Item -Path ".\bin\Release\net8.0\publish\app_offline.htm" -Force

Write-Host "Done! Refresh your browser at http://localhost:8080/swagger" -ForegroundColor Green
