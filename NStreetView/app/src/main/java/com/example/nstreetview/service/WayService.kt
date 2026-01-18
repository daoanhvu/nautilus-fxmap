package com.example.nstreetview.service

import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.model.CrossingPointDto
import com.example.nstreetview.data.model.MapMatchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

class WayService(private val appDatabase: AppDatabase) {
  fun metersToLat(m: Double) = m / 111_320.0
  fun metersToLon(m: Double, lat: Double) = m / (111_320.0 * cos(Math.toRadians(lat)))

  private fun lonLatToMercator(lon: Double, lat: Double): Pair<Double, Double> {
    val x = lon * 20037508.34 / 180.0
    var y = ln(tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180)
    y *= 20037508.34 / 180.0
    return x to y
  }

  private fun bearing(ax: Double, ay: Double, bx: Double, by: Double): Double {
    val deg = Math.toDegrees(atan2(bx - ax, by - ay))
    return if (deg < 0) deg + 360 else deg
  }

  fun geoBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val alpha1 = Math.toRadians(lat1)
    val alpha2 = Math.toRadians(lat2)
    val lambda = Math.toRadians(lon2 - lon1)

    val x = sin(lambda) * cos(alpha2)
    val y = cos(alpha1) * sin(alpha2) -
        sin(alpha1) * cos(alpha2) * cos(lambda)

    return (Math.toDegrees(atan2(x, y)) + 360) % 360
  }


  private fun parseWkt(wkt: String): List<Pair<Double, Double>> {
    return wkt.substringAfter("(").substringBefore(")")
      .split(',').mapNotNull {
        val p = it.trim().split(" ")
        if (p.size >= 2) p[0].toDouble() to p[1].toDouble() else null
      }
  }

  fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    // Earth radius in meters
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) *
        cos(Math.toRadians(lat2)) *
        sin(dLon / 2).pow(2)
    return 2 * r * asin(sqrt(a))
  }

  fun isAhead(carLat: Double, carLon: Double, heading: Double, crossLat: Double, crossLon: Double): Boolean {
    val bearingToCross = geoBearing(carLat, carLon, crossLat, crossLon)
    val diff = abs(bearingToCross - heading)
    return diff !in 45.0..315.0
  }

  suspend fun match(lat: Double, lng: Double, prevLat: Double?, prevLng: Double?): MapMatchResult? =
    withContext(Dispatchers.IO) {
      val deltaLat = 30.0 / 111320.0
      val deltaLng = deltaLat / cos(Math.toRadians(lat))
      val ids = appDatabase.wayRtreeDao().queryCandidateWays(lat - deltaLat, lat + deltaLat,
        lng - deltaLng, lng + deltaLng)
      val (px, py) = lonLatToMercator(lng, lat)
      var best: MapMatchResult? = null
      var bestDist = Double.MAX_VALUE

      for (id in ids) {
        val wkt = appDatabase.wayGeometryDao().getGeometryText(id) ?: continue
        val wayWidth = appDatabase.streetWayDao().getWidth(id) ?: 6.0F
        val halfWidth = wayWidth / 2.0F
        val pts = parseWkt(wkt)

        for (i in 0 until pts.size - 1) {
          val i1 = i + 1
          val (ax, ay) = lonLatToMercator(pts[i].first, pts[i].second)
          val (bx, by) = lonLatToMercator(pts[i1].first, pts[i1].second)

          val vx = bx - ax
          val vy = by - ay
          val wx = px - ax
          val wy = py - ay
          val t = ((vx * wx + vy * wy) / (vx * vx + vy * vy)).coerceIn(0.0, 1.0)
          val projx = ax + t * vx
          val projy = ay + t * vy
          val dist = hypot(px - projx, py - projy)

          if (dist < bestDist && dist <= halfWidth) {
            val segBearing = bearing(ax, ay, bx, by)
            val vehBearing = if (prevLat != null && prevLng != null) {
              val (hx, hy) = lonLatToMercator(prevLng, prevLat)
              bearing(hx, hy, px, py)
            } else {
              segBearing
            }

            val forward = abs(segBearing - vehBearing) < 90
            bestDist = dist
            best = MapMatchResult(id, dist, segBearing, forward)
          }
        }
      }
      best
    }

  suspend fun detectCrossingWarning(carLat: Double, carLon: Double, heading: Double): CrossingPointDto? {
    val deltaLat = metersToLat(400.0)
    val deltaLon = metersToLon(400.0, carLat)

    val candidates = appDatabase.crossingPointDao().findNearby(
      carLat - deltaLat,
      carLat + deltaLat,
      carLon - deltaLon,
      carLon + deltaLon
    )

//    val distance = haversine(carLat, carLon, candidates[0].lat, candidates[0].lon)
//    android.util.Log.d(TAG, "Caculated distance: $distance")

    return candidates
      .filter { haversine(carLat, carLon, it.lat, it.lon) < 400.0 }
      .firstOrNull { isAhead(carLat, carLon, heading, it.lat, it.lon) }
      ?. let { entity -> CrossingPointDto(
        id = entity.id,
        lat = entity.lat,
        lon = entity.lon,
        signalType = entity.signalType)
      }
  }

  companion object {
    const val TAG = "WayService"
  }
}