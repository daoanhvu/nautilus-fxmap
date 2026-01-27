package com.example.nstreetview.ui.component

import com.example.nstreetview.data.model.MapPoint

class MapViewPort(
  var minLat: Float,
  var maxLat: Float,
  var minLon: Float,
  var maxLon: Float,
  var widthPx: Int,
  var heightPx: Int
  ) {

  fun project(lat: Float, lon: Float): MapPoint {
    val x = ((lon - minLon) / (maxLon - minLon)) * widthPx
    val y = heightPx -
        ((lat - minLat) / (maxLat - minLat)) * heightPx

    return MapPoint(lat, lon, x, y)
  }
}