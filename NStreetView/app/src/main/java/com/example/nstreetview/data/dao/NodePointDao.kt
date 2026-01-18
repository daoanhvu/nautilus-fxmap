package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.NodePointEntity

@Dao
interface NodePointDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addNodePoint(way: NodePointEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(ways: List<NodePointEntity>)

  @Query("SELECT * FROM nsv_nodes WHERE id = :pId")
  fun getNodeById(pId: Long): NodePointEntity?

  @Query("SELECT * FROM nsv_nodes WHERE id > 0")
  fun getAllNodes(): List<NodePointEntity>
}