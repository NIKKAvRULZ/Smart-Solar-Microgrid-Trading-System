package com.team.smartsolar.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartSolarLocal.db";
    // Bumped to version 4 to trigger onUpgrade and drop the mock booking table
    private static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_SESSION = "Session";
    public static final String TABLE_USER_CACHE = "UserProfileCache";
    public static final String TABLE_STATION_CACHE = "StationCache";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSessionTable = "CREATE TABLE " + TABLE_SESSION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nic TEXT, " +
                "role TEXT, " +
                "token TEXT, " +
                "expiresAt INTEGER)";

        String createUserCacheTable = "CREATE TABLE " + TABLE_USER_CACHE + " (" +
                "nic TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "email TEXT, " +
                "role TEXT, " +
                "status TEXT, " +
                "lastSyncedAt INTEGER)";

        String createStationCacheTable = "CREATE TABLE " + TABLE_STATION_CACHE + " (" +
                "stationId TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "capacity REAL, " +
                "availableSlots INTEGER, " +
                "status TEXT, " +
                "lastSyncedAt INTEGER)";

        db.execSQL(createSessionTable);
        db.execSQL(createUserCacheTable);
        db.execSQL(createStationCacheTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION_CACHE);
        db.execSQL("DROP TABLE IF EXISTS mock_bookings"); // Cleans up the old mock table
        onCreate(db);
    }

    public boolean saveSession(String nic, String role, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("role", role);
        values.put("token", token);

        db.execSQL("DELETE FROM " + TABLE_SESSION);
        long result = db.insert(TABLE_SESSION, null, values);
        return result != -1;
    }

    public boolean saveUserProfile(String nic, String name, String email, String role, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("name", name);
        values.put("email", email);
        values.put("role", role);
        values.put("status", status);

        long result = db.insertWithOnConflict(TABLE_USER_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

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

    public String getSessionToken() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT token FROM " + TABLE_SESSION + " LIMIT 1", null);
        String token = null;
        if (cursor.moveToFirst()) {
            token = cursor.getString(0);
        }
        cursor.close();
        return token;
    }

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

    public Cursor getAllCachedStations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STATION_CACHE, null);
    }

    public void logoutUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESSION);
        db.execSQL("DELETE FROM " + TABLE_USER_CACHE);
    }

    public String getSessionNic() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nic FROM " + TABLE_SESSION + " LIMIT 1", null);
        String nic = null;
        if (cursor.moveToFirst()) {
            nic = cursor.getString(0);
        }
        cursor.close();
        return nic;
    }
}