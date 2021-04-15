package com.nautilus.fxmap.view;

import com.nautilus.fxmap.geo.GeoBoundary;
import com.nautilus.fxmap.geo.MapProjection;
import com.nautilus.fxmap.geo.MapSource;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

import java.util.List;

public abstract class FXMapView extends Pane implements MapProjection.MapBoundChangeListener {

    protected final double DRAGGING_OFFSET = 4.0;

    protected double mouseXPosOnClick;
    protected double mouseYPosOnClick;
    protected double lastMouseXPos;
    protected double lastMouseYPos;
    protected double previousMouseXPosOnClick;
    protected double previousMouseYPosOnClick;

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

    protected void initializeEventHandlers() {
        cvMovingObjects.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
        cvMovingObjects.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMouseClicked);
        cvMovingObjects.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        cvMovingObjects.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragging);
    }

    protected void onMouseDragging(MouseEvent evt) {
        double xOffset = lastMouseXPos - evt.getSceneX();
        double yOffset = lastMouseYPos - evt.getSceneY();
        lastMouseXPos = evt.getSceneX();
        lastMouseYPos = evt.getSceneY();
        final double translateX = cvMovingObjects.getTranslateX();
        final double translateY = cvMovingObjects.getTranslateY();
        mapCanvas.setTranslateX(translateX + (-1 * xOffset));
        mapCanvas.setTranslateY(translateY + (-1 * yOffset));
        cvMovingObjects.setTranslateX(translateX + (-1 * xOffset));
        cvMovingObjects.setTranslateY(translateY + (-1 * yOffset));
    }

    protected void onMouseDragged(double fromX, double fromY, double toX, double toY) {
        mapCanvas.setTranslateX(0.0);
        mapCanvas.setTranslateY(0.0);
        cvMovingObjects.setTranslateX(0.0);
        cvMovingObjects.setTranslateY(0.0);

        // Get the current bounds
        GeoBoundary currentBounds = mapContent.getBounds();

        Point2D minXY = mapContent.xyToLocation(0.0 - toX, 0.0 - toY);
        Point2D maxXY = mapContent.xyToLocation(mapCanvas.getWidth() - toX, mapCanvas.getHeight() - toY);

        // TODO: If currentBounds is the maximum bounds then we don't need to calculate new bounds
        // Build a new bounds object
        GeoBoundary newBounds = new GeoBoundary(minXY.getX(), maxXY.getX(), minXY.getY(), maxXY.getY());
        setExtent(newBounds);
    }

    protected void onMouseClicked(MouseEvent evt) {
        if (evt.getButton().equals(MouseButton.PRIMARY)) {
            if (evt.getClickCount() > 1) {
                zoomIn();
            }
            if (evt.getClickCount() == 1) {
                mouseXPosOnClick = evt.getSceneX();
                mouseYPosOnClick = evt.getSceneY();
                lastMouseXPos = mouseXPosOnClick;
                lastMouseYPos = mouseYPosOnClick;
            }
        }
        if (evt.getButton().equals(MouseButton.SECONDARY)) {
            if (evt.getClickCount() > 1) {
                zoomOut();
            }
        }
    }

    protected void onMouseReleased(MouseEvent evt) {
        if(evt.getX() < (mouseXPosOnClick + DRAGGING_OFFSET)
                && evt.getX() > (mouseXPosOnClick - DRAGGING_OFFSET)
                && evt.getY() < (mouseYPosOnClick + DRAGGING_OFFSET)
                && evt.getY() > (mouseYPosOnClick - DRAGGING_OFFSET)) {
            previousMouseXPosOnClick = mouseXPosOnClick;
            previousMouseYPosOnClick = mouseYPosOnClick;
            return;
        }
        onMouseDragged(0.0, 0.0, cvMovingObjects.getTranslateX(), cvMovingObjects.getTranslateY());
    }

    protected abstract void setExtent(GeoBoundary newBounds);
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
