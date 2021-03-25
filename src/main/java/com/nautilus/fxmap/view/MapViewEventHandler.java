package com.nautilus.fxmap.view;

public interface MapViewEventHandler {
    void onMapViewEventListenersChanged(int count, int type);
    void onMapBoundChanged(double zoomLevel);
}
