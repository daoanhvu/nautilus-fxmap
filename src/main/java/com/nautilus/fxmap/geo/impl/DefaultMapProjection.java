package com.nautilus.fxmap.geo.impl;

import com.nautilus.fxmap.geo.GeoBoundary;
import com.nautilus.fxmap.geo.GeoMappedPoint;
import com.nautilus.fxmap.geo.GeoPoint;
import com.nautilus.fxmap.geo.MapProjection;
import com.nautilus.fxmap.geo.MapSource;
import com.nautilus.fxmap.map.JSMapBridge;
import com.nautilus.fxmap.map.impl.DefaultJSMapBridge;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO: This class is just correct for US or somewhere in the WEST
 * we need to fix the formulas in this class so that they are correct worldwide
 * https://en.wikipedia.org/wiki/Web_Mercator_projection
 * @author davu
 */
public class DefaultMapProjection extends MapProjection {

    /*
     *                       NW (North East)
     *      |---------------------X
     *      |                     |
     *      X---------------------|
     *  SE (South West)
     *
     */
    private final GeoMappedPoint southWest;
    private final GeoMappedPoint northEast;
    private boolean hasCenteringTransforms;
    private boolean matchingAspectRatio = true;
    private boolean useMercatorProjection = false;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final JSMapBridge jsMapBridge;

    private double xRatio;
    private double yRatio;

    // DATA SECTION
//    private List<Way> ways;
    private Color background;

    public GeoBoundary getBounds() {
        GeoBoundary bounds = new GeoBoundary(
            jsMapBridge.getSouthWest().getLon(),
            jsMapBridge.getNorthEast().getLon(),
            jsMapBridge.getSouthWest().getLat(),
            jsMapBridge.getNorthEast().getLat());
        return new GeoBoundary(bounds);
    }

    public DefaultMapProjection() {
        this.zoomLevel = 8.0;
        this.southWest = new GeoMappedPoint();
        this.northEast = new GeoMappedPoint();
        jsMapBridge = new DefaultJSMapBridge();
        jsMapBridge.setEventHandler(this);
        jsMapBridge.setZoomLevel(this.zoomLevel);
    }

    public DefaultMapProjection(double width, double height) {
        this.zoomLevel = 8.0;
        this.southWest = new GeoMappedPoint(29.03, -127.49, 0, height);
        this.northEast = new GeoMappedPoint(48.80, -64.56, width, 0);
        jsMapBridge = new DefaultJSMapBridge();
        jsMapBridge.setEventHandler(this);
        jsMapBridge.setZoomLevel(this.zoomLevel);
    }

    @Override
    public void initializeJSBridge(WebEngine webEngine) {
        jsMapBridge.setWebEngine(webEngine);
        jsMapBridge.setJavaScriptEnabled(true);
        jsMapBridge.initWebEngine();
    }

    @Override
    public MapSource switchMapSource(MapSource target) {
        jsMapBridge.switchMapSource(target);
        return jsMapBridge.getMapSource();
    }

    public void moveWebMapToCenter() {
        GeoPoint center = new GeoPoint((southWest.getLatitude() + northEast.getLatitude()) / 2.0,
                (northEast.getLongitude() + southWest.getLongitude()) / 2.0);
        jsMapBridge.setCenter(center.getLon(), center.getLat());
        jsMapBridge.executeScript("document.panTo()");
        // The map moved to a new bounds so we have to calculate the bounds for this area again
        GeoBoundary bounds = new GeoBoundary(
                jsMapBridge.getSouthWest().getLon(),
                jsMapBridge.getNorthEast().getLon(),
                jsMapBridge.getSouthWest().getLat(),
                jsMapBridge.getNorthEast().getLat());
        setBounds(bounds);
    }

    public void zoomIn() {
        zoomLevel = jsMapBridge.getZoomLevel();
        if(zoomLevel < MAX_ZOOM_LEVEL) {
            zoomLevel = zoomLevel + 1.0;
            jsMapBridge.setZoomLevel(zoomLevel);
            jsMapBridge.executeScript("document.boundsAndZoom()");
//            // The map moved to a new bounds so we have to calculate the bounds for this area again
//            GeoBoundary bounds = new GeoBoundary(
//                    jsMapBridge.getSouthWest().getLon(),
//                    jsMapBridge.getNorthEast().getLon(),
//                    jsMapBridge.getSouthWest().getLat(),
//                    jsMapBridge.getNorthEast().getLat());
//            setBounds(bounds);
        }
    }

    public void zoomOut() {
        zoomLevel = jsMapBridge.getZoomLevel();
        if(zoomLevel > MIN_ZOOM_LEVEL) {
            zoomLevel = zoomLevel - 1.0;
            jsMapBridge.setZoomLevel(zoomLevel);
            jsMapBridge.executeScript("document.boundsAndZoom()");
//            // The map moved to a new bounds so we have to calculate the bounds for this area again
//            GeoBoundary bounds = new GeoBoundary(
//                    jsMapBridge.getSouthWest().getLon(),
//                    jsMapBridge.getNorthEast().getLon(),
//                    jsMapBridge.getSouthWest().getLat(),
//                    jsMapBridge.getNorthEast().getLat());
//            setBounds(bounds);
        }
    }

    private void marshalFromJSBridge() {
        zoomLevel = jsMapBridge.getZoomLevel();
        southWest.setLatitude(jsMapBridge.getSouthWest().getLat());
        southWest.setLongitude(jsMapBridge.getSouthWest().getLon());
        northEast.setLatitude(jsMapBridge.getNorthEast().getLat());
        northEast.setLongitude(jsMapBridge.getNorthEast().getLon());
    }

    public Point2D locationToXYMercator(double lat, double lon) {
        double x1 = WORLD_MAP_WIDTH * (Math.pow(2.0, zoomLevel)) * (lon + Math.PI) / TWO_PI;
        double y1 = WORLD_MAP_HEIGHT * (Math.pow(2.0, zoomLevel)) * (Math.PI - Math.log(Math.tan(PI_OVER_4 + lat/2.0))) / TWO_PI ;
        return new Point2D(x1, y1);
    }

    @Override
    public Point2D locationToXY(double lat, double lon) {
        if(useMercatorProjection) {
            return locationToXYMercator(lat, lon);
        }

        double x1 = (lon - southWest.getLongitude() ) / xRatio;
        double y1 = (northEast.getLatitude() - lat) / yRatio;
        return new Point2D(x1, y1);
    }

    public Point2D xyToLocationMercator(double x, double y) {
        double lon = (x * TWO_PI) /(WORLD_MAP_WIDTH * (Math.pow(2.0, zoomLevel))) - Math.PI;
        double num1 = y * TWO_PI / (WORLD_MAP_HEIGHT * (Math.pow(2.0, zoomLevel)));
        double num2 = -(num1 - Math.PI);
        double lat = 2.0 * (Math.atan(Math.exp(num2)) - PI_OVER_4);
        return new Point2D(lon, lat);
    }

    @Override
    public Point2D xyToLocation(double x, double y) {
        if(useMercatorProjection)
            return xyToLocationMercator(x, y);

        double lon = southWest.getLongitude() + xRatio * x;
        double lat = northEast.getLatitude() - yRatio * y;
        return new Point2D(lon, lat);
    }

    //TODO: correct this method
    public double distanceToDuration(double distanceInKm, double speedInKPH) {
        if(speedInKPH == 0.0)
            return 0.0;
        return distanceInKm / speedInKPH;
    }

    public void render(GraphicsContext graphicsContext) { }

    public double getWidth() {
        return northEast.getX() - southWest.getX() + 1;
    }

    public double getHeight() {
        return southWest.getY() - northEast.getY() + 1;
    }

    public double centerScreenX() {
        return (northEast.getX() + southWest.getX()) / 2.0;
    }

    public double centerScreenY() {
        return (southWest.getY() + northEast.getY()) / 2.0;
    }

    public void setScreenSize(int width, int height) {
        lock.writeLock().lock();
        southWest.setXY(0.0, height);
        northEast.setXY(width, 0.0);
        hasCenteringTransforms = false;
        try {
            setTransforms(false);
        } catch (Exception e) {
//            LOG.error(e);
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void translateCenterInPixel(double dx, double dy) {
        double dLng = (dx + 1) * xRatio;
        double dLat = (dy + 1) * yRatio;

        double newCenterLat = jsMapBridge.getCenterLat() + dLat;
        double newCenterLng = jsMapBridge.getCenterLng() - dLng;
        jsMapBridge.setCenter(newCenterLng, newCenterLat);
        jsMapBridge.executeScript("document.panTo()");
    }

    private void setTransforms(boolean b) {
        xRatio = Math.abs(northEast.getLongitude() - southWest.getLongitude()) / (Math.abs(northEast.getX() - southWest.getX()) + 1);
        yRatio = Math.abs(southWest.getLatitude() - northEast.getLatitude()) / (Math.abs(southWest.getY() - northEast.getY()) + 1);
    }

    public void setPreBounds(GeoBoundary preBounds) {
        GeoPoint center = preBounds.getCenter();
        jsMapBridge.setCenter(center.getLon(), center.getLat());
        jsMapBridge.executeScript("document.panTo()");

//        // The map moved to a new bounds so we have to calculate the bounds for this area again
//        GeoBoundary bounds = new GeoBoundary(
//                jsMapBridge.getSouthWest().getLon(),
//                jsMapBridge.getNorthEast().getLon(),
//                jsMapBridge.getSouthWest().getLat(),
//                jsMapBridge.getNorthEast().getLat());
//        setBounds(bounds);
    }

    public void setBounds(GeoBoundary value) {
        lock.writeLock().lock();
        try {
            southWest.setLongitude(value.getMinX());
            southWest.setLatitude(value.getMinY());
            northEast.setLatitude(value.getMaxY());
            northEast.setLongitude(value.getMaxX());
            setTransforms(true);
//            fireMapBoundsListenerMapBoundsChanged(old, new GeoBoundary(this.bounds));
        } catch (Exception e) {
//            LOG.error(e);
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void fireMapBoundsListenerMapBoundsChanged(GeoBoundary old, GeoBoundary bounds1) {
        for(MapBoundChangeListener l: this.mapBoundsListenerList) {
            l.onMapBoundChange(old, bounds1);
        }
    }

    @Override
    public String toString() {
        return "DefaultMapProjection's SW: " + southWest + ", NE: " + northEast + "\n"
                + "WebMap's bound: " + jsMapBridge.toString();
    }

    @Override
    public void onWebMapInitialized() {
        marshalFromJSBridge();
        setTransforms(true);
    }

    @Override
    public void onWebMapPropertiesChanged() {
        marshalFromJSBridge();
        setTransforms(true);
        fireMapBoundsListenerMapBoundsChanged(null, null);
    }
}
