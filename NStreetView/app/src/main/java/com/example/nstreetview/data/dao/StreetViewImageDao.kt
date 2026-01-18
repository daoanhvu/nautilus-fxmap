package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.StreetViewImage

@Dao
interface StreetViewImageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertImage(image: StreetViewImage)

  @Query("SELECT * FROM nsv_images")
  suspend fun getAllImages(): List<StreetViewImage>
}
