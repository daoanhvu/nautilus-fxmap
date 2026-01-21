package com.example.nstreetview.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun StreetMapCanvas() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    drawRect(color = Color.LightGray)
  }
}