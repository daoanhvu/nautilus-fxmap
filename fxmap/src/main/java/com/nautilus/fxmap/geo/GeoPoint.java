package com.nautilus.fxmap.geo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoPoint implements Serializable {
    private double lat;
    private double lon;

    /**
     * So far we haven't used this member variable
     */
    @JsonIgnore
    private double elevation;

    public GeoPoint() {}

    /**
     * TODO: Please change the order of passed parameter to (lon, lat)
     * @param lat
     * @param lon
     */
    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Canonicalize the current latitude and longitude values such that:
     * <pre>
     * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
     * </pre>
     */
    private void canonicalize() {
        lat = (lat + 180.0) % 360.0;
        if (lat < 0.0) lat += 360;
        lat -= 180;

        if (lat > 90) {
            lat = 180 - lat;
            lon += 180;
        } else if (lat < -90) {
            lat = -180 - lat;
            lon += 180;
        }

        lon = ((lon + 180.0) % 360.0);
        if (lon <= 0) lon += 360;
        lon -= 180;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLocation(double lng, double lat) {
        this.lon = lng;
        this.lat = lat;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GeoPoint))
            return false;
        GeoPoint other = (GeoPoint) obj;
        return (lon == other.lon) && (lat == other.lat);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Math.abs(lat));
        buffer.append((lat >= 0.0) ? 'N' : 'S');
        buffer.append(';');
        buffer.append(Math.abs(lon));
        buffer.append((lon >= 0.0) ? 'E' : 'W');
        buffer.append(';');
        return buffer.toString();
    }
}
