package com.example.nstreetview

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.nstreetview.data.model.LocationInfo
import com.example.nstreetview.data.model.VehicleInfo
import com.google.gson.Gson
import java.util.LinkedList

class NSVApplication: Application() {
  lateinit var vehicleInfo: VehicleInfo
    private set

  private lateinit var prefs: SharedPreferences
  private val gson = Gson()

  override fun onCreate() {
    super.onCreate()
    prefs = getSharedPreferences("nsv_vehicle", Context.MODE_PRIVATE)
    loadOrInitVehicleInfo()
  }

  private fun loadOrInitVehicleInfo() {
    val vehicleJson = prefs.getString("current_vehicle", null)
    vehicleInfo = if (vehicleJson != null) {
      // If data exists in SharedPreferences, load it
      gson.fromJson(vehicleJson, VehicleInfo::class.java)
    } else {
      // If no data exists, create a new default object
      val newVehicle = VehicleInfo(
        number = "51K-123.45",
        manufacturer = "Honda",
        model = "Vision",
        owner = "Default User",
        vehicleType = 1, // e.g., 1 for motorcycle
        locations = LinkedList<LocationInfo>() // Start with an empty queue
      )
      // Save the newly created object to SharedPreferences for next time
      saveVehicleInfo(newVehicle)
      newVehicle
    }
  }

  fun saveVehicleInfo(vehicleInfo: VehicleInfo) {
    this.vehicleInfo = vehicleInfo // Update the in-memory instance
    val vehicleJson = gson.toJson(vehicleInfo)
    prefs.edit().putString("current_vehicle", vehicleJson).apply()
  }
}