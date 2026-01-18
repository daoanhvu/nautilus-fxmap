package com.example.nstreetview.repository

import android.content.Context
import androidx.annotation.RawRes
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.service.OsmParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OsmRepository(context: Context, private val database: AppDatabase = AppDatabase.getDatabase(context)) {

  private val streetWayDao = database.streetWayDao()
  private val wayGeometryDao = database.wayGeometryDao()
  private val wayRtreeDao = database.wayRtreeDao()
  private val crossPointDao = database.crossingPointDao()
  private val parser = OsmParser()

  suspend fun importOsmDataFromResource(@RawRes resourceId: Int, context: Context) {
    withContext(Dispatchers.IO) {
      // Open the raw resource
      val inputStream = context.resources.openRawResource(resourceId)

      // Parse the data
      val parsedData = parser.parse(inputStream)

      // Use a transaction to ensure all data is inserted or none at all
      database.runInTransaction {
        streetWayDao.insertAll(parsedData.ways)
        wayGeometryDao.insertAll(parsedData.geometries)
        wayRtreeDao.insertAll(parsedData.rtreeEntries)
        crossPointDao.insertAll(parsedData.crossingEntries)
      }
    }
  }
}