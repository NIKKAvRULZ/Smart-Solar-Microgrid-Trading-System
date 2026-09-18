# Project Setup & Hosting Guide

This document outlines the exact steps required to set up the development environment, run the Web API locally, and successfully host it on a Windows IIS Server.

---

## 1. Required Installations

Before starting, ensure the following software is installed on your Windows machine:

1. **Visual Studio 2022** (or later)
   - Include the **"ASP.NET and web development"** workload during installation.
2. **.NET 8.0 SDK**
   - Required to build and run the application locally.
3. **.NET 8.0 Windows Server Hosting Bundle**
```
https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/runtime-aspnetcore-8.0.31-windows-hosting-bundle-installer
```
   - **CRITICAL:** This is required for IIS to host ASP.NET Core applications.
   - Download the "Hosting Bundle" installer from the official Microsoft .NET 8.0 downloads page.
   - After installation, open an Administrator Command Prompt and run `iisreset` to ensure the module is registered.
4. **MongoDB (Cloud or Local)**
   - The project uses MongoDB. You can use MongoDB Atlas (Cloud) or a local MongoDB Server.

---

## 2. Setting Up the Project (Visual Studio)

1. Clone the repository to your local machine.
2. Open `web-service/SolarGrid.Api.sln` using Visual Studio 2022.
3. Ensure the active startup project is set to `SolarGrid.Api`.
4. Right-click the solution in Solution Explorer and click **Restore NuGet Packages**.

### Configuring the Database Connection
Open `appsettings.json` in the `SolarGrid.Api` project and ensure your MongoDB connection string is placed in the configuration:

```json
"SolarGridDatabase": {
  "ConnectionString": "YOUR_MONGODB_CONNECTION_STRING",
  "DatabaseName": "SolarGridDB",
  "StationsCollectionName": "Stations",
  "UsersCollectionName": "Users",
  "BookingsCollectionName": "Bookings"
}
```

---

## 3. How to Run Locally (Development)

Running the application locally is the fastest way to test code changes.

**Option A: Using Visual Studio**
1. Press `F5` (or click the green "Play" button at the top).
2. Visual Studio will launch a temporary local web server (usually at `https://localhost:7087` or similar).
3. The Swagger UI documentation will automatically open in your browser.

**Option B: Using the CLI**
1. Open a terminal (Command Prompt or PowerShell) and navigate to `web-service/SolarGrid.Api`.
2. Run the command:
   ```cmd
   dotnet run
   ```
3. Look at the terminal output to find the local URL (e.g., `http://localhost:5000`).
4. Open your browser and navigate to `http://localhost:5000/swagger`.

---

## 4. How to Host on Windows IIS (Production)

The assignment requires the API to be hosted on a Windows IIS Server using the FAT service architecture.

### Step 4.1: Enable IIS on Windows
1. Open **Control Panel** > **Programs and Features**.
2. Click **Turn Windows features on or off** on the left sidebar.
3. Check **Internet Information Services** (ensure "Web Management Tools" and "World Wide Web Services" are checked).
4. Click OK and wait for the installation to finish.

### Step 4.2: Publish the Application
To host the app, we must first compile it into a "Production" publish folder.

1. Open PowerShell and navigate to `web-service/SolarGrid.Api`.
2. Run the provided publish script to safely compile the code without file lock errors:
   ```powershell
   .\publish.ps1
   ```
   *(This will compile the application and place the output in `bin\Release\net8.0\publish`)*

### Step 4.3: Configure the IIS Website
1. Press the Windows Key, type **IIS Manager**, and open it.
2. On the left pane, right-click on **Sites** and select **Add Website**.
3. Fill in the details:
   - **Site name:** `SolarGridApi`
   - **Physical path:** Browse and select the `publish` folder created in Step 4.2. (e.g., `...\SolarGrid.Api\bin\Release\net8.0\publish`).
   - **Port:** Set the port to `8080` (or any available port).
4. Click OK.

### Step 4.4: Application Pool Settings
1. In IIS Manager, click on **Application Pools** on the left pane.
2. Find the pool associated with your site (`SolarGridApi`).
3. Right-click it and select **Basic Settings**.
4. Change the **.NET CLR version** to **No Managed Code** (ASP.NET Core runs out-of-process and manages itself).
5. Click OK.

### Step 4.5: Important Permissions Note
If your `publish` folder is located in a restrictive directory (like an external drive or a user's Documents folder), IIS may throw an `HTTP Error 500.19` (Configuration file is not well-formed XML).
- **The Fix:** Move the `publish` folder to `C:\inetpub\wwwroot\SolarGridApi` and update the physical path in IIS to point to this new location. Windows automatically grants the correct permissions to the `C:\inetpub\wwwroot` folder.

---

## 5. Testing the IIS Deployment

Once IIS is configured, open your web browser and navigate to:
`http://localhost:8080/swagger/index.html`

If you see the Swagger UI listing the `/api/v1/Users`, `/api/v1/Bookings`, and `/api/v1/Stations` endpoints, your IIS deployment is fully successful!
