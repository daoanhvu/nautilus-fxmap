package com.nautilus.fxmap.geo;

public class GeoBoundary {
    private double maxY;
    private double minY;
    private double maxX;
    private double minX;

    public GeoBoundary() {}

    public GeoBoundary(double _minX, double _maxX, double _minY, double _maxY) {
        this.maxX = _maxX;
        this.minX = _minX;
        this.maxY = _maxY;
        this.minY = _minY;
    }

    public GeoBoundary(final GeoBoundary bounds) {
        this.maxY = bounds.maxY;
        this.minY = bounds.minY;
        this.maxX = bounds.maxX;
        this.minX = bounds.minX;
    }

    public GeoBoundary setBounds(double _minX, double _maxX, double _minY, double _maxY) {
        this.maxX = _maxX;
        this.minX = _minX;
        this.maxY = _maxY;
        this.minY = _minY;
        return this;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getMedian(int dimension) {
        if(dimension == 0) {
            return (minX + maxX) / 2.0;
        }
        return (minY + maxY) / 2.0;
    }

    public GeoPoint getCenter() {
        return new GeoPoint((minY + maxY) / 2.0, (minX + maxX) / 2.0);
    }

    public boolean isContain(GeoBoundary other) {
        boolean midMinX = (other.minX >= this.minX) && ( other.minX < this.maxX );
        boolean midMaxX = (other.maxX <= this.maxX) && ( other.maxX > this.minX );
        boolean midMinY = (other.minY >= this.minY) && ( other.minY < this.maxY );
        boolean midMaxY = (other.maxY <= this.maxY) && ( other.maxY > this.minY );
        return midMinX && midMaxX && midMinY && midMaxY;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GeoBoundary))
            return false;
        GeoBoundary other = (GeoBoundary) obj;
        return (minY == other.minY)
                && (maxY == other.maxY)
                && (minX == other.minX)
                && (maxX == other.maxX);
    }

    @Override
    public String toString() {
        return "NorthWest: (" + maxY + ", " + minX + ") SouthEast: (" + minY + ", " + maxX + ")";
    }
}
