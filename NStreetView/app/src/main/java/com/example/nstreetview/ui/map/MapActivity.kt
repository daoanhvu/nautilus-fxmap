package com.example.nstreetview.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.nstreetview.NSVApplication
import com.example.nstreetview.repository.OsmRepository
import com.example.nstreetview.R
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.model.LocationInfo
import com.example.nstreetview.data.model.VehicleInfo
import com.example.nstreetview.service.WayService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapActivity: ComponentActivity() {
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback

  private lateinit var vehicleInfo: VehicleInfo
  private var lastLocation: LocationInfo? = null
  private lateinit var appDatabase: AppDatabase
  private lateinit var wayService: WayService

  // Use mutableStateOf to hold the location and speed data for the Compose UI
  private var locationText by mutableStateOf("Location: Not available")
  private var speedText by mutableStateOf("Speed: 0 m/s")

  // Register for the activity result to handle the permission request
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        // If permission is granted, start location updates
        startLocationUpdates()
      } else {
        // Handle the case where the user denies the permission
        locationText = "Location permission denied"
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vehicleInfo = (application as NSVApplication).vehicleInfo
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    appDatabase = AppDatabase.getDatabase(this)
    wayService = WayService(appDatabase)

    // Define the callback to receive location updates
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let { location ->
          // Update the UI with the new location and speed
          locationText = "Location: ${location.latitude}, ${location.longitude}"
          // Speed is in meters/second
          speedText = "Speed: ${location.speed} m/s with bearing: ${location.bearing}"
          val locInfo = LocationInfo(location.time, location.latitude,
            location.longitude,
            location.bearing, location.speed)

          lifecycleScope.launch {
            lastLocation?.let {
              val wayResult = wayService.match(locInfo.lat, locInfo.lon,
                lastLocation?.lat,
                lastLocation?.lon)
              wayResult ?. let {
                withContext(Dispatchers.Main) {
                  // Update UI state here
                }
              }
            }
          }

          if (vehicleInfo.locations.size >= 120) {
            vehicleInfo.locations.remove()
          }
          vehicleInfo.locations.add(locInfo)
          lastLocation = locInfo
        }
      }
    }

    // Check and request location permissions
    when {
      ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED -> {
        // If permission is already granted, start location updates
        startLocationUpdates()
      }
      else -> {
        // Otherwise, request the permission
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
      }
    }

    // Set the content of the activity to a Composable function
    setContent {
      LocationDisplay(locationText, speedText, appDatabase)
    }

  }

  private fun startLocationUpdates() {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
      .build()

    // Check for permission again before requesting updates
    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      // This is a redundant check, but required by the IDE
      return
    }
    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper()
    )
  }

  override fun onPause() {
    super.onPause()
    // Stop location updates when the activity is not in the foreground
    fusedLocationClient.removeLocationUpdates(locationCallback)
    appDatabase.close()
  }
}

@Composable
fun LocationDisplay(location: String, speed: String, appDatabase: AppDatabase) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val osmRepository = remember { OsmRepository(context, appDatabase) }

  fun importExampleData() {
    scope.launch {
      try {
        // This runs the parsing and database insertion on an IO thread
        osmRepository.importOsmDataFromResource(R.raw.pham_van_dong_kha_van_can, context)
        // Show a success message on the main thread
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "Map data imported successfully!", Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        e.printStackTrace()
        // Show an error message on the main thread
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "Error importing map data: ${e.message}", Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  Column(modifier = Modifier.padding(16.dp)) {
    Text(text = location)
    Text(text = speed)
    Spacer(modifier = Modifier.height(24.dp)) // Add space between the buttons

    // Button for "Import Data"
    Button(
      onClick = {
        importExampleData()
      },
      modifier = Modifier
        .fillMaxWidth(0.8f) // Match the width of the other button
        .height(60.dp) // Match the height
    ) {
      Text(text = "Import example map data", fontSize = 18.sp)
    }
  }
}