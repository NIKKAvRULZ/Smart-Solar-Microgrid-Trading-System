package com.team.smartsolar.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import com.team.smartsolar.models.Booking;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartSolarLocal.db";
    // Bumped to version 3 to trigger the onUpgrade method and build the new table
    private static final int DATABASE_VERSION = 3;

    // Table Names
    public static final String TABLE_SESSION = "Session";
    public static final String TABLE_USER_CACHE = "UserProfileCache";
    public static final String TABLE_STATION_CACHE = "StationCache";
    public static final String TABLE_MOCK_BOOKINGS = "mock_bookings"; // NEW

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Session Table (Holds login token and role)
        String createSessionTable = "CREATE TABLE " + TABLE_SESSION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nic TEXT, " +
                "role TEXT, " +
                "token TEXT, " +
                "expiresAt INTEGER)";

        // 2. User Profile Cache (Holds prosumer profile data)
        String createUserCacheTable = "CREATE TABLE " + TABLE_USER_CACHE + " (" +
                "nic TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "email TEXT, " +
                "role TEXT, " +
                "status TEXT, " +
                "lastSyncedAt INTEGER)";

        // 3. Station Cache (Holds map marker data for offline/fast loads)
        String createStationCacheTable = "CREATE TABLE " + TABLE_STATION_CACHE + " (" +
                "stationId TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "capacity REAL, " +
                "availableSlots INTEGER, " +
                "status TEXT, " +
                "lastSyncedAt INTEGER)";

        // 4. Mock Bookings Table (Temporary persistence until API is ready)
        String createMockBookingsTable = "CREATE TABLE " + TABLE_MOCK_BOOKINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "station TEXT, " +
                "date TEXT, " +
                "time TEXT, " +
                "energy TEXT, " +
                "status TEXT)";

        // Execute the SQL to create the tables
        db.execSQL(createSessionTable);
        db.execSQL(createUserCacheTable);
        db.execSQL(createStationCacheTable);
        db.execSQL(createMockBookingsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOCK_BOOKINGS);
        onCreate(db);
    }

    // --- 1. Save Login Session ---
    public boolean saveSession(String nic, String role, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("role", role);
        values.put("token", token);
        // Clear any old session first so only one user is logged in at a time
        db.execSQL("DELETE FROM " + TABLE_SESSION);

        long result = db.insert(TABLE_SESSION, null, values);
        return result != -1; // Returns true if insertion was successful
    }

    // --- 2. Save Prosumer Profile Cache ---
    public boolean saveUserProfile(String nic, String name, String email, String role, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("name", name);
        values.put("email", email);
        values.put("role", role);
        values.put("status", status);

        // Using replace so if the NIC already exists, it updates the cached profile
        long result = db.insertWithOnConflict(TABLE_USER_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // --- 3. Save Station Map Data Cache ---
    public boolean saveStation(String stationId, String name, double latitude, double longitude, double capacity, int availableSlots, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stationId", stationId);
        values.put("name", name);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("capacity", capacity);
        values.put("availableSlots", availableSlots);
        values.put("status", status);

        long result = db.insertWithOnConflict(TABLE_STATION_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // --- 4. Save Mock Booking ---
    public void saveMockBooking(String station, String date, String startTime, String endTime, String energy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("station", station);
        values.put("date", date);
        values.put("time", startTime + " - " + endTime);
        values.put("energy", energy);
        values.put("status", "Pending"); // Default status

        db.insert(TABLE_MOCK_BOOKINGS, null, values);
        db.close();
    }

    // --- 5. Fetch Mock Bookings ---
    public List<Booking> getMockBookings() {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOCK_BOOKINGS + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String station = cursor.getString(cursor.getColumnIndexOrThrow("station"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String energy = cursor.getString(cursor.getColumnIndexOrThrow("energy"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                bookingList.add(new Booking(station, date, time, energy, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bookingList;
    }

    // --- 6. Read Session Data ---
    // Retrieves the active token to send to Sasmitha's API
    public String getSessionToken() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT token FROM " + TABLE_SESSION + " LIMIT 1", null);
        String token = null;
        if (cursor.moveToFirst()) {
            token = cursor.getString(0); // 0 is the first column in our SELECT statement
        }
        cursor.close();
        return token;
    }

    // Retrieves the role to determine which dashboard to show
    public String getSessionRole() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM " + TABLE_SESSION + " LIMIT 1", null);
        String role = null;
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        return role;
    }

    // --- 7. Read Station Data for Maps ---
    // Returns a Cursor containing all cached stations to draw map markers
    public Cursor getAllCachedStations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STATION_CACHE, null);
    }

    // --- 8. Clear Session (For Logout) ---
    public void logoutUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESSION);
        db.execSQL("DELETE FROM " + TABLE_USER_CACHE);
    }// --- Update Mock Booking ---
    public void updateMockBooking(String station, String originalDate, String originalTime, String newDate, String newTime, String newEnergy) {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("date", newDate);
        values.put("time", newTime);
        values.put("energy", newEnergy);

        // Update the row where the station, date, and time match the original booking
        db.update(TABLE_MOCK_BOOKINGS, values, "station=? AND date=? AND time=?",
                new String[]{station, originalDate, originalTime});
        db.close();
    }

    // --- Delete Mock Booking ---
    public void deleteMockBooking(String station, String date, String time) {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();

        // Delete the row matching this booking
        db.delete(TABLE_MOCK_BOOKINGS, "station=? AND date=? AND time=?",
                new String[]{station, date, time});
        db.close();
    }
}