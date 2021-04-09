package com.nautilus.fxmap.view;

import com.nautilus.fxmap.geo.GeoBoundary;
import com.nautilus.fxmap.geo.MapProjection;
import com.nautilus.fxmap.geo.MapSource;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public abstract class FXMapView extends Pane implements MapProjection.MapBoundChangeListener {

    protected Canvas mapCanvas;
    protected Canvas cvMovingObjects;

    // For webview
    protected WebView webView;

    protected MapProjection mapContent;
    protected AnimationTimer animation;
    protected boolean animationStarted = false;
    protected ContextMenu activeContextMenu = null;

    public FXMapView() {
        mapCanvas = new Canvas();
        cvMovingObjects = new Canvas();
        this.getChildren().add(mapCanvas);
        this.getChildren().add(cvMovingObjects);
    }

    protected void loadWebViewMap() {
        Platform.runLater(() -> {
            if(webView != null) {
                return;
            }
            FXMapView.this.webView = new WebView();
            FXMapView.this.getChildren().add(0, webView);
            FXMapView.this.mapContent.initializeJSBridge(webView.getEngine());

            FXMapView.this.widthProperty().addListener((obs, oldVal, newVal) -> {
                webView.setPrefWidth(newVal.doubleValue());
                webView.setPrefHeight(this.getHeight());
            });

            FXMapView.this.heightProperty().addListener((obs, oldVal, newVal) -> {
                webView.setPrefWidth(getWidth());
                webView.setPrefHeight(newVal.doubleValue());
            });
        });
    }

    public void initialize() {
        mapCanvas.widthProperty().bind(this.widthProperty());
        mapCanvas.heightProperty().bind(this.heightProperty());
        cvMovingObjects.widthProperty().bind(this.widthProperty());
        cvMovingObjects.heightProperty().bind(this.heightProperty());
    }

    protected abstract void render();

    @Override
    public void onMapBoundChange(GeoBoundary old, GeoBoundary bounds1) {
        Platform.runLater(() -> {
            List<? extends MapViewEventHandler> mapViewEventHandlers = getMapViewEventHandlers();
            if(mapViewEventHandlers != null) {
                mapViewEventHandlers.parallelStream().forEach(h -> h.onMapBoundChanged(mapContent.getZoomLevel()));
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

    public void onSwitchMapSource(MapSource mapSource) {
        final MapSource afterChangeMS = this.mapContent.switchMapSource(mapSource);
        Platform.runLater(() -> {
            List<? extends MapViewEventHandler> mapViewEventHandlers = getMapViewEventHandlers();
            if(mapViewEventHandlers != null) {
                mapViewEventHandlers.parallelStream().forEach(h -> h.onMapSourceChange(afterChangeMS));
            }
        });
    }

    public MapProjection getMapContent() {
        return mapContent;
    }

    public void setMapContent(MapProjection mapContent) {
        this.mapContent = mapContent;
    }

    public abstract List<? extends MapViewEventHandler> getMapViewEventHandlers();

    public abstract void setMapViewEventHandler(List<? extends MapViewEventHandler> mapViewEventHandlers);
}
