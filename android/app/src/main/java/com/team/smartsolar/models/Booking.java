package com.team.smartsolar.models;

public class Booking {
    private String id;
    private String stationName;
    private String date;
    private String time;
    private String energyAmount;
    private String status;

    public Booking(String id, String stationName, String date, String time, String energyAmount, String status) {
        this.id = id;
        this.stationName = stationName;
        this.date = date;
        this.time = time;
        this.energyAmount = energyAmount;
        this.status = status;
    }

    public String getId(){return id;}
    public String getStationName() { return stationName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getEnergyAmount() { return energyAmount; }
    public String getStatus() { return status; }
}