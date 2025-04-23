package com.plcoding.videoplayercompose

import android.graphics.Paint.Align
import android.media.audiofx.Equalizer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Equalizer() {
    val equalizer = remember { Equalizer(0, 0) }
    equalizer.enabled = true
    val numBands: Int = equalizer.numberOfBands.toInt()
    val bandLevels = remember { mutableStateListOf<Int>() }

    // Initialize band levels
    bandLevels.clear()
    for (i in 0 until numBands) {
        bandLevels.add(equalizer.getBandLevel(i.toShort()).toInt())
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray,
                    titleContentColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                ),
                title = { Text("Equalizer") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(numBands) { i ->
                val bandIndex = i
                val frequency = equalizer.getCenterFreq(bandIndex.toShort())

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Display frequency label
                    Text(text = "Band $bandIndex (${frequency / 1000} kHz)")

                    // Slider for band level control
                    Slider(
                        value = bandLevels[bandIndex].toFloat(),
                        onValueChange = { newLevel ->
                            bandLevels[bandIndex] = newLevel.toInt()
                            equalizer.setBandLevel(
                                bandIndex.toShort(),
                                newLevel.toInt().toShort()
                            )
                        },
                        valueRange = -1500f..1500f, // Typical range for equalizer gains in millibels
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

