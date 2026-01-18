package com.example.nstreetview.ui.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nstreetview.ui.theme.NStreetViewTheme

class CameraActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      NStreetViewTheme {
        Surface(
          modifier = Modifier.Companion.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          CameraScreen()
        }
      }
    }
  }
}