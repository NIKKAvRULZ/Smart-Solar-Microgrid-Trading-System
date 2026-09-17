package com.team.smartsolar.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartSolarLocal.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_SESSION = "Session";
    public static final String TABLE_USER_CACHE = "UserProfileCache";
    public static final String TABLE_STATION_CACHE = "StationCache";

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

        // Execute the SQL to create the tables
        db.execSQL(createSessionTable);
        db.execSQL(createUserCacheTable);
        db.execSQL(createStationCacheTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION_CACHE);
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

}