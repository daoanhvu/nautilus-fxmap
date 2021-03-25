package com.nautilus.fxmap.geo;

public class GeoMappedPoint {
    private double latitude;
    private double longitude;
    private double x;
    private double y;

    public GeoMappedPoint() { }

    public GeoMappedPoint(double lat, double lon, double x, double y) {
        this.latitude = lat;
        this.longitude = lon;
        this.x = x;
        this.y = y;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "LatLng: (" + latitude + ", " + longitude + ") XY: (" + x + ", " + y + ")";
    }
}
