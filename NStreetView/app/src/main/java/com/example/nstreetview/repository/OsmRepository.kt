package com.example.nstreetview.repository

import android.content.Context
import androidx.annotation.RawRes
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.entity.CrossingWayEntity
import com.example.nstreetview.service.OsmParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OsmRepository(context: Context, private val database: AppDatabase = AppDatabase.getDatabase(context)) {

  private val streetWayDao = database.streetWayDao()
  private val wayGeometryDao = database.wayGeometryDao()
  private val wayRtreeDao = database.wayRtreeDao()
  private val crossPointDao = database.crossingPointDao()
  private val crossingWaysDao = database.crossingWaysDao()
  private val parser = OsmParser()

  suspend fun importOsmDataFromResource(@RawRes resourceId: Int, context: Context) {
    withContext(Dispatchers.IO) {
      // Open the raw resource
      val inputStream = context.resources.openRawResource(resourceId)

      // Parse the data
      val parsedData = parser.parse(inputStream)
      val crossingWaysList = mutableListOf<CrossingWayEntity>()
      for (pId in parsedData.crossingWays.keys) {
        val wayIds = parsedData.crossingWays[pId]
        for (wayId in wayIds!!) {
          val crossingWayEntity = CrossingWayEntity(pId, wayId)
          crossingWaysList.add(crossingWayEntity)
        }
      }

      // Use a transaction to ensure all data is inserted or none at all
      database.runInTransaction {
        streetWayDao.insertAll(parsedData.ways)
        wayGeometryDao.insertAll(parsedData.geometries)
        wayRtreeDao.insertAll(parsedData.rtreeEntries)
        crossPointDao.insertAll(parsedData.crossingEntries)
        crossingWaysDao.insertAll(crossingWaysList)
      }
    }
  }
}