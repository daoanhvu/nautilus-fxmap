package com.example.nstreetview.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class OsmParserTest {

  private lateinit var parser: OsmParser

  @Before
  fun setUp() {
    // Create a new instance of the parser before each test
    parser = OsmParser()
  }

  @Test
  fun `parse correctly transforms OSM XML to database entities`() {
    // 1. Arrange: Create a sample OSM XML as a string.
    // This XML contains one way ("Main Street") with two nodes.
    val osmXmlString = """
            <osm version="0.6">
                <node id="101" lat="10.8523" lon="106.7324"/>
                <node id="102" lat="10.8525" lon="106.7326"/>
                <node id="103" lat="10.9999" lon="106.9999"/> 
                <way id="201">
                    <nd ref="101"/>
                    <nd ref="102"/>
                    <tag k="highway" v="residential"/>
                    <tag k="name" v="Main Street"/>
                    <tag k="oneway" v="yes"/>
                    <tag k="lanes" v="2"/>
                </way>
                <way id="202">
                    <nd ref="103"/>
                    <tag k="highway" v="service"/>
                </way>
            </osm>
        """.trimIndent()

    // Convert the string to an InputStream, which the parser expects.
    val inputStream = ByteArrayInputStream(osmXmlString.toByteArray())

    // 2. Act: Call the parse function.
    val parsedData = parser.parse(inputStream)

    // 3. Assert: Verify that the parsed data is correct.
    Assert.assertEquals("Should find 2 ways", 2, parsedData.ways.size)
    Assert.assertEquals("Should generate 2 geometry entries", 2, parsedData.geometries.size)
    Assert.assertEquals("Should generate 2 R-tree entries", 2, parsedData.rtreeEntries.size)

    // --- Assertions for the first way ("Main Street") ---
    val mainStreetWay = parsedData.ways.find { it.id == 201L }
    Assert.assertNotNull("Main Street way (ID 201) should exist", mainStreetWay)
    Assert.assertEquals("residential", mainStreetWay?.highway)
    Assert.assertEquals("Main Street", mainStreetWay?.name)
    Assert.assertEquals(true, mainStreetWay?.oneway)
    Assert.assertEquals(2, mainStreetWay?.lanes)

    // Assert geometry for the first way
    val mainStreetGeom = parsedData.geometries.find { it.wayId == 201L }
    Assert.assertNotNull("Geometry for way 201 should exist", mainStreetGeom)
    Assert.assertEquals(
      "LINESTRING (106.7324 10.8523, 106.7326 10.8525)",
      mainStreetGeom?.geom
    )

    // Assert R-tree bounding box for the first way
    val mainStreetRtree = parsedData.rtreeEntries.find { it.id == 201L }
    Assert.assertNotNull("R-tree entry for way 201 should exist", mainStreetRtree)
    Assert.assertEquals(10.8523, mainStreetRtree?.minLat)
    Assert.assertEquals(10.8525, mainStreetRtree?.maxLat)
    Assert.assertEquals(106.7324, mainStreetRtree?.minLon)
    Assert.assertEquals(106.7326, mainStreetRtree?.maxLon)

    // --- Assertions for the second, simpler way ---
    val serviceWay = parsedData.ways.find { it.id == 202L }
    Assert.assertNotNull("Service way (ID 202) should exist", serviceWay)
    Assert.assertEquals("service", serviceWay?.highway)
    Assert.assertTrue("Name should be null for service way", serviceWay?.name == null)
  }

  @Test
  fun `parse handles empty input stream gracefully`() {
    // Arrange
    val emptyInputStream = ByteArrayInputStream("".toByteArray())

    // Act
    val parsedData = parser.parse(emptyInputStream)

    // Assert
    Assert.assertTrue("Ways list should be empty for empty input", parsedData.ways.isEmpty())
    Assert.assertTrue("Geometries list should be empty", parsedData.geometries.isEmpty())
    Assert.assertTrue("R-tree entries list should be empty", parsedData.rtreeEntries.isEmpty())
  }

  @Test
  fun `parse handles way with missing or invalid nodes`() {
    // Arrange: A way that references a node (104) that doesn't exist.
    val osmXmlString = """
            <osm version="0.6">
                <node id="101" lat="10.8523" lon="106.7324"/>
                <way id="201">
                    <nd ref="101"/>
                    <nd ref="104"/> 
                    <tag k="highway" v="residential"/>
                </way>
            </osm>
        """.trimIndent()
    val inputStream = ByteArrayInputStream(osmXmlString.toByteArray())

    // Act
    val parsedData = parser.parse(inputStream)

    // Assert: The geometry should only contain the valid node.
    Assert.assertEquals(1, parsedData.geometries.size)
    val geometry = parsedData.geometries.first()
    Assert.assertEquals("LINESTRING (106.7324 10.8523)", geometry.geom)
  }
}