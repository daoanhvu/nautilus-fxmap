package com.example.nstreetview

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.repository.OsmRepository
import com.example.nstreetview.ui.camera.CameraActivity
import com.example.nstreetview.ui.map.MapActivity
import com.example.nstreetview.ui.theme.NStreetViewTheme
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      NStreetViewTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          mainScreen()
        }
      }
    }
  }
}

@Composable
fun mainScreen() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val appDatabase = AppDatabase.getDatabase(context)
  val osmRepository = remember { OsmRepository(context, appDatabase) }

  fun exportDataToJson() {
    scope.launch(Dispatchers.IO) {
      // 1. Fetch data from the database
      val images = appDatabase.streetViewImageDao().getAllImages()
      if (images.isEmpty()) {
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "No data to export.", Toast.LENGTH_SHORT).show()
        }
        return@launch
      }

      // 2. Convert data to JSON string
      val gson = Gson()
      val jsonString = gson.toJson(images)

      // 3. Save the JSON string to a file in shared storage
      val resolver = context.contentResolver
      val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())
      val fileName = "NStreetView_$date.json"

      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
        // Store in the 'Documents' directory to be with the images
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/NStreetView-Image")
      }

      val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
      if (uri != null) {
        try {
          resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
              writer.write(jsonString)
            }
          }
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Data exported to Documents/NStreetView-Image", Toast.LENGTH_LONG).show()
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
          }
        }
      } else {
        withContext(Dispatchers.Main) {
          Toast.makeText(context, "Failed to create file in media store.", Toast.LENGTH_LONG).show()
        }
      }

    }
  }

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


  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp), // Add some padding around the column
    verticalArrangement = Arrangement.Center, // Center the buttons vertically
    horizontalAlignment = Alignment.CenterHorizontally // Center the buttons horizontally
  ) {
    // Button to open CameraActivity
    Button(
      onClick = {
        // Create an intent to start CameraActivity
        val intent = Intent(context, CameraActivity::class.java)
        context.startActivity(intent)
      },
      modifier = Modifier
        .fillMaxWidth(0.8f) // Make the button 80% of the screen width
        .height(60.dp) // Set a larger, touch-friendly height
    ) {
      Text(text = "Record Street", fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(24.dp)) // Add space between the buttons

    // Button for "Export Data"
    Button(
      onClick = {
        exportDataToJson()
      },
      modifier = Modifier
        .fillMaxWidth(0.8f) // Match the width of the other button
        .height(60.dp) // Match the height
    ) {
      Text(text = "Export Data", fontSize = 18.sp)
    }

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

    Spacer(modifier = Modifier.height(24.dp)) // Add space between the buttons

    // Button to test map activity
    Button(
      onClick = {
        val intent = Intent(context, MapActivity::class.java)
        context.startActivity(intent)
      },
      modifier = Modifier
        .fillMaxWidth(0.8f) // Match the width of the other button
        .height(60.dp) // Match the height
    ) {
      Text(text = "Open Map View", fontSize = 18.sp)
    }
  }
}