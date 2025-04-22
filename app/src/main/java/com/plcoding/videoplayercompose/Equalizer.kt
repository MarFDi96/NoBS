package com.plcoding.videoplayercompose

import android.graphics.Paint.Align
import android.media.audiofx.Equalizer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Equalizer() {
    val equalizer = remember { Equalizer(0, 0) }
    equalizer.enabled = true
    val numBands = equalizer.numberOfBands
    val bandLevels = remember { mutableStateListOf<Int>() }

    // Initialize band levels
    bandLevels.clear()
    for (i in 0 until numBands) {
        bandLevels.add(equalizer.getBandLevel(i.toShort()).toInt())
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp) // Add horizontal and vertical padding
                .align(Alignment.TopCenter), // Align Column to the Top Center
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 0 until numBands) {
                val bandIndex = i
                val frequency = equalizer.getCenterFreq(bandIndex.toShort())

                // Display frequency label
                Text(text = "Band $bandIndex (${frequency / 1000} kHz)")

                // Slider for band level control
                Slider(
                    value = bandLevels[bandIndex].toFloat(),
                    onValueChange = { newLevel ->
                        bandLevels[bandIndex] = newLevel.toInt()
                        equalizer.setBandLevel(bandIndex.toShort(), newLevel.toInt().toShort())
                    },
                    valueRange = -1500f..1500f // Typical range for equalizer gains in millibels
                )
            }
        }
    }
}

