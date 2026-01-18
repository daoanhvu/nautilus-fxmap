package com.nautilus.fxmap.geo;

import com.nautilus.fxmap.view.WebMapEventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.web.WebEngine;

import java.util.ArrayList;
import java.util.List;

public abstract class MapProjection implements WebMapEventHandler {

    protected static final double WORLD_MAP_WIDTH = 1.6803870967746433E7;
//    private static final double WORLD_MAP_HEIGHT = 9158485.811522977;
    protected static final double WORLD_MAP_HEIGHT = 1.6803870967746433E7;
    protected static final double WORLD_MAP_Y_CENTER = WORLD_MAP_HEIGHT / 2.0;
    protected static final double WORLD_MAP_X_CENTER = WORLD_MAP_WIDTH / 2.0;
    protected static final double LAT_DEGREE_PER_PIXEL  = 1.857318573185773E-5;
    protected static final double LAT_RADIAN_PER_PIXEL  = Math.toRadians(LAT_DEGREE_PER_PIXEL);
    protected static final double X_OFFSET = 3839642.5064526405;
    protected static final double Y_OFFSET = 3770126.9953415995;
    protected static final double MAX_LATITUDE = 85.051129;
    protected static final double TWO_PI = Math.PI * 2.0;
    protected static final double PI_OVER_4 = Math.PI / 4.0;
    protected static final double MIN_ZOOM_LEVEL = 3.0;
    protected static final double MAX_ZOOM_LEVEL = 20.0;

    protected final List<MapBoundChangeListener> mapBoundsListenerList = new ArrayList<>();
    protected double zoomLevel = 0.0;
    protected Rectangle2D screenArea;

    public void addEventListener(MapBoundChangeListener listener) {
        if(listener != null) {
            mapBoundsListenerList.add(listener);
        }
    }

    public abstract void setScreenSize(int w, int h);

    public abstract void render(GraphicsContext g);

    public abstract GeoBoundary getBounds();

    public abstract void setPreBounds(GeoBoundary preBounds);

    public interface MapBoundChangeListener {
        void onMapBoundChange(GeoBoundary old, GeoBoundary bounds1);
    }

    public MapProjection() { }

    public abstract void translateCenterInPixel(double dx, double dy);

    /**
     * latitude and longitude in to be in radian
     */
    public abstract Point2D locationToXY(double lat, double lon);
    public abstract Point2D xyToLocation(double x, double y);
    public Point2D lineEquation(double x1, double y1, double x2, double y2) {
        double a = (y2 - y1)/(x2 - x1);
        double b = y1 - a * x1;
        return new Point2D(a, b);
    }

    public abstract void initializeJSBridge(WebEngine webEngine);

    public abstract MapSource switchMapSource(MapSource target);

    public abstract void zoomIn();
    public abstract void zoomOut();

    public double getZoomLevel() {
        return zoomLevel;
    }

    public double getWidth() {
        return screenArea.getWidth();
    }

    public double getHeight() {
        return screenArea.getHeight();
    }

    public double centerScreenX() {
        return (screenArea.getMinX() + screenArea.getMaxX()) / 2.0;
    }

    public double centerScreenY() {
        return (screenArea.getMinY() + screenArea.getMaxY()) / 2.0;
    }
}
