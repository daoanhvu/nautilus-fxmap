package com.example.nstreetview.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
  var isRecording by mutableStateOf(false)
  var currentVideoId by mutableStateOf<String?>(null)
}
