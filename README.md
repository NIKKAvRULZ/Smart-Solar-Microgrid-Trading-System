# Smart-Solar-Microgrid-Trading-System

A robust, 3-tier distributed energy trading platform. This project allows Prosumers to reserve solar energy from Grid Nodes via a mobile app, and Grid Operators to manage the system via a web dashboard.

## System Architecture

1. **Backend API (.NET 8 & IIS)**: 
   - A robust Web API built with .NET 8 using the Repository pattern and FAT service architecture. 
   - Hosted on a Windows IIS Server.
   - Connected to a MongoDB database.
2. **Web Dashboard (React)**: 
   - A sleek, responsive dashboard built with React, Vite, and TailwindCSS for Grid Operators to monitor nodes and reservations.
3. **Mobile App (Android Native)**: 
   - A native Android application for Prosumers to book energy, manage their profiles, and generate QR ticket codes.
   - Includes a secure Grid Operator QR Scanning mode to finalize energy transactions.

## Local Network Deployment

When presenting or testing the system, it's configured to run seamlessly across your local Wi-Fi network.

### Handling IP Changes (DHCP)
If your laptop's local IP address changes (e.g. you move from home to university), you must update the IPs in two places so the Frontend and Mobile App can find the IIS Backend:

1. **Find your IP**: Open Command Prompt and type `ipconfig` to find your IPv4 Address (e.g. `192.168.x.x`).
2. **Update Frontend**: Edit `Frontend/.env`:
   ```env
   VITE_API_BASE_URL=http://<YOUR_NEW_IP>:8080/api
   ```
3. **Update Android App**: Edit `android/local.properties`:
   ```properties
   api.base.url=http://<YOUR_NEW_IP>:8080
   ```

### Firewall Configuration
If devices on your network cannot reach the IIS Server or React Frontend, open an Administrator PowerShell and run:
```powershell
New-NetFirewallRule -DisplayName "SmartSolar Ports" -Direction Inbound -LocalPort 8080,5173 -Protocol TCP -Action Allow
```

**For full, step-by-step setup and IIS deployment instructions, please read [SETUP.md](SETUP.md).**