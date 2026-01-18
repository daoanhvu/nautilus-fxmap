package com.example.nstreetview.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.entity.StreetViewImage
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val IMAGE_FILENAME_FORMAT = "yyyyMMddHHmmssSSS"

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel() ) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var recording: Recording? by remember { mutableStateOf(null) }
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
  val imageCapture: MutableState<ImageCapture?> = remember { mutableStateOf(null) }
  val videoCapture: MutableState<VideoCapture<Recorder>?> = remember { mutableStateOf(null) }

  // Get a reference to the database and a coroutine scope
  val db = remember { AppDatabase.getDatabase(context) }
  val scope = rememberCoroutineScope()

  var hasCamPermission by remember {
    mutableStateOf(
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }
  var hasAudioPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
    )
  }
  var hasLocationPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED )
  }
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
      hasCamPermission = permissions[Manifest.permission.CAMERA] ?: hasCamPermission
      hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
      hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPermission
    }
  )
  LaunchedEffect(key1 = true) {
    launcher.launch(
      arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
  }

  fun takePhoto() {
    val currentVideoId = viewModel.currentVideoId
    if (currentVideoId == null) {
      Toast.makeText(context, "Must start recording to take a picture.", Toast.LENGTH_SHORT).show()
      return
    }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
      // Optionally, re-launch the permission request
      launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
      return // Stop if no permission
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
      if (location == null) {
        Toast.makeText(context, "Location not available. Photo not saved.", Toast.LENGTH_SHORT).show()
        return@addOnSuccessListener
      }

      // Create timestamp and new image name
      val timestamp = System.currentTimeMillis()
      val imageName = "NSV_" + SimpleDateFormat(IMAGE_FILENAME_FORMAT, Locale.US).format(timestamp)

      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
          put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NStreetView-Image")
        }
      }

      val outputOptionsBuilder = ImageCapture.OutputFileOptions
        .Builder(
          context.contentResolver,
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          contentValues
        ).setMetadata(ImageCapture.Metadata().apply {
        this.location = location
      })

      val outputOptions = outputOptionsBuilder.build()

      imageCapture.value?.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
          override fun onError(exc: ImageCaptureException) {
            Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
          }

          override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            scope.launch {
              db.streetViewImageDao().insertImage(
                StreetViewImage(
                  videoId = currentVideoId,
                  imageName = imageName,
                  lon = location.longitude,
                  lat = location.latitude,
                  imageTime = timestamp
                )
              )
            }
            val msg = "Photo capture succeeded: ${output.savedUri} at "
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          }
        }
      )
    }
  }

  fun startRecording() {
    val videoId = UUID.randomUUID().toString()
    viewModel.currentVideoId = videoId
    val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        .format(System.currentTimeMillis()) + "_$videoId"
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, name)
      put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
          put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NStreetView-Video")
      }
    }

    val mediaStoreOutputOptions = MediaStoreOutputOptions
        .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        .setContentValues(contentValues)
        .build()

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
      != PackageManager.PERMISSION_GRANTED) {
      return
    }
    recording = videoCapture.value?.output?.prepareRecording(context, mediaStoreOutputOptions)?.withAudioEnabled()?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
    }
    viewModel.isRecording = true
  }

  fun stopRecording() {
    recording?.stop()
    viewModel.isRecording = false
    viewModel.currentVideoId = null
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (hasCamPermission && hasAudioPermission && hasLocationPermission) {
      AndroidView(
        factory = { context ->
          val previewView = PreviewView(context)
          val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.HIGHEST, Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
          )

          val recorder = Recorder.Builder()
              .setQualitySelector(qualitySelector)
              .build()
          videoCapture.value = VideoCapture.withOutput(recorder)
          imageCapture.value = ImageCapture.Builder().build()

          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            preview.setSurfaceProvider(previewView.surfaceProvider)
            try {
              cameraProvider.unbindAll()
              cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture.value,
                videoCapture.value
              )
            } catch (e: Exception) {
              // handle exceptions
            }
          }, ContextCompat.getMainExecutor(context))
          previewView
        },
        modifier = Modifier.fillMaxSize()
      )
      if (viewModel.isRecording) {
        Button(
          onClick = { takePhoto() },
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
            .navigationBarsPadding()
        ) {
          Text("Take Pic")
        }
        Button(
          onClick = { stopRecording() },
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .navigationBarsPadding()
        ) {
          Text("Stop")
        }
      } else {
        Button(
          onClick = { startRecording() },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
            .navigationBarsPadding()
        ) {
          Text("Start")
        }
      }
    } else {
      Text(text = "Camera and Audio and Location permissions are required to use this feature.")
    }
  }
}
