package com.plcoding.videoplayercompose

import android.net.Uri
import androidx.media3.common.MediaItem

data class AudioItem(
    val contentUri: Uri,
    val mediaItem: MediaItem,
    val name: String
)
