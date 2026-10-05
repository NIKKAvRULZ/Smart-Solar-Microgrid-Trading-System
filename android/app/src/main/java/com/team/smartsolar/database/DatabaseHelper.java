// -----------------------------------------------------------------------------
// File: DatabaseHelper.java
// Author: Nithika Perera
// Purpose: Implements SQLiteOpenHelper for local session and cache persistence,
// fulfilling the pure native Android local storage requirement without using Room.
// -----------------------------------------------------------------------------

package com.team.smartsolar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.team.smartsolar.models.NodeResponse;
import com.team.smartsolar.models.ProsumerProfile;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartSolarLocal.db";
    // Incremented to 3 to force Android to safely rebuild the tables with the new token columns
    private static final int DATABASE_VERSION = 5;

    // Tables
    private static final String TABLE_SESSION = "Session";
    private static final String TABLE_PROFILE = "UserProfileCache";
    private static final String TABLE_STATION = "StationCache";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Session Table (Tracks logged-in user, role, and JWT token)
        db.execSQL("CREATE TABLE " + TABLE_SESSION + " (" +
                "nic TEXT PRIMARY KEY, " +
                "role TEXT, " +
                "token TEXT)");

        // 2. Profile Cache Table (Stores user details for offline viewing)
        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                "nic TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "email TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "isActive INTEGER, " +
                "lastSyncedAt INTEGER)");

        // 3. Station Cache Table (Stores grid nodes for the map and booking dropdown)
        db.execSQL("CREATE TABLE " + TABLE_STATION + " (" +
                "stationId TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "capacity REAL, " +
                "availableSlots INTEGER, " +
                "lastSyncedAt INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATION);
        onCreate(db);
    }

    // ==========================================
    // SESSION MANAGEMENT
    // ==========================================

    public void saveSession(String nic, String role, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESSION); // Keep only one active session

        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("role", role);
        values.put("token", token);
        db.insert(TABLE_SESSION, null, values);
        db.close();
    }

    public String getSessionNic() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nic FROM " + TABLE_SESSION + " LIMIT 1", null);
        String nic = null;
        if (cursor.moveToFirst()) {
            nic = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return nic;
    }

    public String getSessionToken() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT token FROM " + TABLE_SESSION + " LIMIT 1", null);
        String token = null;
        if (cursor.moveToFirst()) {
            token = cursor.getString(0);
        }
        cursor.close();
        db.close();
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
        db.close();
        return role;
    }

    public void logoutUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SESSION);
        db.execSQL("DELETE FROM " + TABLE_PROFILE); // Clear sensitive profile cache on logout
        db.close();
    }

    // ==========================================
    // PROFILE CACHE
    // ==========================================

    public void cacheProfile(String nic, ProsumerProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nic", nic);
        values.put("name", profile.getFullName());
        values.put("email", profile.getEmail());
        values.put("phone", profile.getPhone());
        values.put("address", profile.getAddress());
        values.put("isActive", profile.isActive() ? 1 : 0);
        values.put("lastSyncedAt", System.currentTimeMillis());

        // Insert or replace ensures we don't get primary key crashes on updates
        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public ProsumerProfile getCachedProfile(String nic) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILE + " WHERE nic = ?", new String[]{nic});

        ProsumerProfile profile = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1;

            profile = new ProsumerProfile(name, email, phone, address);
            profile.setActive(isActive);
        }
        cursor.close();
        db.close();
        return profile;
    }

    // ==========================================
    // STATION CACHE
    // ==========================================

    public void cacheStations(List<NodeResponse> stations) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Clear old cache to remove deleted stations
            db.execSQL("DELETE FROM " + TABLE_STATION);

            for (NodeResponse station : stations) {
                ContentValues values = new ContentValues();
                values.put("stationId", station.getId() != null ? station.getId() : station.getStationId());
                values.put("name", station.getName() != null ? station.getName() : station.getStationName());
                values.put("latitude", station.getLatitude());
                values.put("longitude", station.getLongitude());
                values.put("capacity", station.getCapacityKWh());
                values.put("availableSlots", station.getAvailableBatterySlots());
                values.put("lastSyncedAt", System.currentTimeMillis());

                db.insert(TABLE_STATION, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<NodeResponse> getCachedStations() {
        List<NodeResponse> stationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STATION, null);

        if (cursor.moveToFirst()) {
            do {
                NodeResponse station = new NodeResponse();
                station.setStationId(cursor.getString(cursor.getColumnIndexOrThrow("stationId")));
                station.setStationName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                station.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                station.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                station.setCapacityKWh(cursor.getDouble(cursor.getColumnIndexOrThrow("capacity")));
                station.setAvailableBatterySlots(cursor.getInt(cursor.getColumnIndexOrThrow("availableSlots")));

                stationList.add(station);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stationList;
    }
}