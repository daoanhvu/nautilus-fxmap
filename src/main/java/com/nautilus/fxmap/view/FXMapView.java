package com.nautilus.fxmap.view;

import com.nautilus.fxmap.geo.GeoBoundary;
import com.nautilus.fxmap.geo.MapProjection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

public abstract class FXMapView extends Pane implements MapProjection.MapBoundChangeListener {
    @FXML
    protected Canvas mapCanvas;

    // For webview
    protected WebView webView;

    protected MapProjection mapContent;
    protected MapViewEventHandler mapViewEventHandler;

    public FXMapView() {}

    protected void loadWebViewMap() {
        Platform.runLater(() -> {
            if(webView != null) {
                return;
            }
            webView = new WebView();
            getChildren().add(0, webView);
            mapContent.initializeJSBridge(webView.getEngine());
        });

        widthProperty().addListener((obs, oldVal, newVal) -> {
            webView.setPrefWidth(newVal.doubleValue());
            webView.setPrefHeight(this.getHeight());
        });

        heightProperty().addListener((obs, oldVal, newVal) -> {
            webView.setPrefWidth(getWidth());
            webView.setPrefHeight(newVal.doubleValue());
        });
    }

    abstract void render();

    @Override
    public void onMapBoundChange(GeoBoundary old, GeoBoundary bounds1) {
        Platform.runLater(() -> {
            if(mapViewEventHandler != null) {
                mapViewEventHandler.onMapBoundChanged(mapContent.getZoomLevel());
            }
        });
        render();
    }

    @Override
    public void resize(double w, double h) {
        super.resize(w, h);
        mapContent.setScreenSize((int) w, (int)h);
        render();
    }

    public void zoomIn() {
        mapContent.zoomIn();
        render();
    }

    public void zoomOut() {
        mapContent.zoomOut();
        render();
    }

    public MapProjection getMapContent() {
        return mapContent;
    }

    public void setMapContent(MapProjection mapContent) {
        this.mapContent = mapContent;
    }

    public MapViewEventHandler getMapViewEventHandler() {
        return mapViewEventHandler;
    }

    public void setMapViewEventHandler(MapViewEventHandler mapViewEventHandler) {
        this.mapViewEventHandler = mapViewEventHandler;
    }
}
