package com.example.nstreetview.service

import com.example.nstreetview.data.entity.CrossingPointEntity
import com.example.nstreetview.data.entity.WayEntity
import com.example.nstreetview.data.entity.WayGeometryEntity
import com.example.nstreetview.data.entity.WayRtreeEntity
import kotlin.math.max
import kotlin.math.min
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

// Data classes to temporarily hold parsed OSM data
data class OsmNode(val id: Long, val lat: Double, val lon: Double, var isCrossing: Boolean, var signalTypes: String?)
data class OsmWay(val id: Long, val tags: Map<String, String>, val nodeRefs: List<Long>)

// A container for all parsed data ready for the database
data class ParsedOsmData(
  val ways: List<WayEntity>,
  val geometries: List<WayGeometryEntity>,
  val rtreeEntries: List<WayRtreeEntity>,
  val crossingEntries: List<CrossingPointEntity>
)


class OsmParser {

  fun parse(input: InputStream): ParsedOsmData {
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()

    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(input, null)

    val nodes = HashMap<Long, OsmNode>()
    val ways = ArrayList<OsmWay>()

    var event = parser.eventType
    var currentOsmNodeType = 0
    var currentNodeId: Long = -1L
    var currentTags = mutableMapOf<String, String>()
    var currentNodeRefs = mutableListOf<Long>()
    var currentWayId: Long? = null

    while (event != XmlPullParser.END_DOCUMENT) {
      when (event) {
        XmlPullParser.START_TAG -> when (parser.name) {
          "node" -> {
            // meet a Node
            val id = parser.getAttributeValue(null, "id")?.toLongOrNull()
            val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
            val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
            if (id != null && lat != null && lon != null) {
              nodes[id] = OsmNode(id, lat, lon, false, null)
              currentNodeId = id
            }
            currentOsmNodeType = 1
          }
          "way" -> {
            currentWayId = parser.getAttributeValue(null, "id").toLongOrNull()
            // meet a way
            currentOsmNodeType = 2
          }
          "nd" -> {
            parser.getAttributeValue(null, "ref")?.toLongOrNull()?.let {
              currentNodeRefs.add(it)
            }
          }
          "tag" -> {
            val k = parser.getAttributeValue(null, "k")
            val v = parser.getAttributeValue(null, "v")

            if (currentOsmNodeType == OSM_WAY) {
              if (k != null && v != null) {
                currentTags[k] = v
              }
            } else if (currentOsmNodeType == OSM_NODE) {
              if (k != null && v != null) {
                if (isCrossing(k, v)) {
                  nodes[currentNodeId]?.isCrossing = true
                  if (v == "traffic_signals") {
                    nodes[currentNodeId]?.signalTypes = v
                  }
                }
              }
            }
          }
        }

        XmlPullParser.END_TAG -> {
          if (parser.name == "way") {
            if (currentWayId != null) {
              ways.add(OsmWay(currentWayId, currentTags, currentNodeRefs))
            }
            currentWayId = null
            currentTags = mutableMapOf()
            currentNodeRefs = mutableListOf()
            currentOsmNodeType = 0
          } else if (parser.name == "node") {
            currentOsmNodeType = 0
          }
        }
      }
      event = parser.next()
    }
    return processOsmData(ways, nodes)
  }

  private fun isCrossing(key: String, tagValue: String): Boolean {
    return (key == "crossing") || ( tagValue == "crossing") || ( tagValue == "traffic_signals" )
  }

  private fun processOsmData(osmWays: List<OsmWay>, osmNodes: Map<Long, OsmNode>): ParsedOsmData {
    val wayEntities = mutableListOf<WayEntity>()
    val geometryEntities = mutableListOf<WayGeometryEntity>()
    val rtreeEntities = mutableListOf<WayRtreeEntity>()
    val crossingEntries = osmNodes.values.filter { it.isCrossing }.map { CrossingPointEntity(it.id, it.lat, it.lon, it.signalTypes) }

    for (way in osmWays) {
      wayEntities.add(
        WayEntity(
          id = way.id,
          highway = way.tags["highway"],
          name = way.tags["name"],
          width = way.tags["width"]?.toDoubleOrNull(),
          oneway = way.tags["oneway"] == "yes",
          lanes = way.tags["lanes"]?.toIntOrNull(),
          maxSpeed = 50,
          minSpeed = 0,
          ref = way.tags["ref"]
        )
      )

      val wayNodes = way.nodeRefs.mapNotNull { osmNodes[it] }
      if (wayNodes.isNotEmpty()) {
        // Create WKT (Well-Known Text) string for geometry
        val geomString = "LINESTRING (" + wayNodes.joinToString(", ") { "${it.lon} ${it.lat}" } + ")"
        geometryEntities.add(WayGeometryEntity(wayId = way.id, geom = geomString))

        // Calculate bounding box for R-tree
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLon = Double.MAX_VALUE
        var maxLon = Double.MIN_VALUE

        wayNodes.forEach { node ->
          minLat = min(minLat, node.lat)
          maxLat = max(maxLat, node.lat)
          minLon = min(minLon, node.lon)
          maxLon = max(maxLon, node.lon)
        }
        rtreeEntities.add(WayRtreeEntity(id = way.id, minLat, maxLat, minLon, maxLon))
      }
    }
    return ParsedOsmData(wayEntities,
      geometryEntities,
      rtreeEntities,
      crossingEntries)
  }

  companion object {
    const val OSM_NODE = 1
    const val OSM_WAY = 2
  }
}