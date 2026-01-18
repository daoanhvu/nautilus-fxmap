package com.nautilus.fxmap.map.impl;

import com.nautilus.fxmap.geo.GeoPoint;
import com.nautilus.fxmap.geo.MapSource;
import com.nautilus.fxmap.map.JSMapBridge;
import com.nautilus.fxmap.view.WebMapEventHandler;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

import java.util.LinkedList;

public class DefaultJSMapBridge extends JSMapBridge {

    public DefaultJSMapBridge() {
        zoomLevel = 3.0;
        mapSource = MapSource.OSM;
    }

    public void initWebEngine() {
        webEngine.getLoadWorker().stateProperty().addListener((ov, old, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("mapBridge", this);
                webEngine.executeScript("document.boundsAndZoom()");

                while(!commands.isEmpty()) {
                    String command = commands.pop();
                    webEngine.executeScript(command);
                }

                if(eventHandler != null) {
                    eventHandler.onWebMapInitialized();
                }
            }
        });

        String htmlMapFile = "webmap";
        if(mapSource.equals(MapSource.OSM)) {
            htmlMapFile = "osm";
        }

        /*
         * TODO: When running the application from jar file, WebEngine could not load URL with parameters.
         *       The work-around solution is removing parameters from the URL
         */
//        webEngine.load(getClass().getResource("/html/" + htmlMapFile + ".html").toExternalForm() +
//                "?zoom=" + zoomLevel + "&lat=" + centerLat + "&lng=" + centerLng);
        webEngine.load(getClass().getResource("/html/" + htmlMapFile + ".html").toExternalForm());
    }

    public void switchMapSource(MapSource ms) {
        if(mapSource.equals(ms)) {
            return;
        }
        mapSource = ms;
        initWebEngine();
    }

    public Object executeScript(String script) {
        if(webEngine == null) {
            final GeoPoint centerPoint = new GeoPoint(centerLat, centerLng);
            if(northEast.equals(southWest) && !southWest.equals(centerPoint)) {
                northEast.setLat(centerLat);
                northEast.setLon(centerLng);

                southWest.setLat(centerLat);
                southWest.setLon(centerLng);
            }
            commands.push(script);
            return null;
        }
        return webEngine.executeScript(script);
    }

    public void fireChangeEvent(double x1, double y1, double x2, double y2) {
        southWest.setLocation(x1, y1);
        northEast.setLocation(x2, y2);
        if(eventHandler != null) {
            eventHandler.onWebMapPropertiesChanged();
        }
    }

    public void setEventHandler(WebMapEventHandler handler) {
        this.eventHandler = handler;
    }

    public GeoPoint getSouthWest() {
        return southWest;
    }

    public void setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public void setSouthWest(GeoPoint sw) {
        this.southWest.setLat(sw.getLat());
        this.southWest.setLon(sw.getLon());
    }

    public void setSouthWest(double lon, double lat) {
        this.southWest.setLat(lat);
        this.southWest.setLon(lon);
    }

    public void setSouthWestLat(double lat) {
        this.southWest.setLat(lat);
    }

    public void setSouthWestLng(double lon) {
        this.southWest.setLon(lon);
    }

    public GeoPoint getNorthEast() {
        return northEast;
    }

    public void setNorthEast(GeoPoint ne) {
        this.northEast.setLon(ne.getLon());
        this.northEast.setLat(ne.getLat());
    }

    public void setNorthEast(double lon, double lat) {
        this.northEast.setLon(lon);
        this.northEast.setLat(lat);
    }

    public void setNorthEastLat(double lat) {
        this.northEast.setLat(lat);
    }

    public void setNorthEastLng(double lon) {
        this.northEast.setLon(lon);
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
    }

    public void setCenter(double centerLng, double centerLat) {
        this.centerLng = centerLng;
        this.centerLat = centerLat;
    }

    public GeoPoint getCenter() {
        return new GeoPoint(centerLat, centerLng);
    }

    public MapSource getMapSource() {
        return mapSource;
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public double geoWidth() {
        return Math.abs(northEast.getLon() - southWest.getLon());
    }

    public double geoHeight() {
        return Math.abs(northEast.getLat() - southWest.getLat());
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    @Override
    public String toString() {
        return "MapSource: " + mapSource + ", Center(" + centerLng + ", " + centerLat + "); Zoom = " + zoomLevel
                + ", Bounds sw: (" + southWest.getLon() + ", " + southWest.getLat() + ")"
                + " ne: (" + northEast.getLon() + ", " + northEast.getLat() + ")"
                + ", last error: " + lastErrorMessage;
    }

    public void setJavaScriptEnabled(boolean b) {
        webEngine.setJavaScriptEnabled(b);
    }
}
