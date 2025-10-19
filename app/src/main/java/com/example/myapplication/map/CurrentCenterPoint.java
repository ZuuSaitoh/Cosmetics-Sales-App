package com.example.myapplication.map;

public class CurrentCenterPoint {
    private double latitude;
    private double longitude;
    private float bearing;

    public CurrentCenterPoint(double latitude, double longitude, float bearing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getBearing() { return bearing; }
    public void setBearing(float bearing) { this.bearing = bearing; }

    @Override
    public String toString() {
        return "CurrentCenterPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", bearing=" + bearing +
                '}';
    }
}
