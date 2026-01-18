package com.nautilus.fxmap.view;

import com.nautilus.fxmap.geo.MapSource;

public interface MapViewEventHandler {
    void onMapViewEventListenersChanged(int count, int type);
    void onMapBoundChanged(double zoomLevel);
    void onMapSourceChange(MapSource ms);
}
