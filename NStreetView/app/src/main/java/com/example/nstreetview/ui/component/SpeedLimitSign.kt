package com.example.nstreetview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeedLimitSign(maxSpeed: Int, modifier: Modifier = Modifier, size: Dp = 150.dp) {
  // The main container for our sign
  Box(
    modifier = modifier
      .size(size) // Set the total size of the sign
      .clip(CircleShape) // Clip the content to a circle
      .background(Color.Red) // Fill the entire circle with red for the border
  ) {
    // The inner white circle
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(size * 0.15f) // Padding to create the thickness of the red border
        .clip(CircleShape)
        .background(Color.White), // Fill the inner circle with white
      contentAlignment = Alignment.Center // Center the text inside
    ) {
      // The text for the speed limit number
      Text(
        text = maxSpeed.toString(),
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        // Dynamically calculate font size based on the sign's size
        fontSize = (size.value * 0.4f).sp
      )
    }
  }
}