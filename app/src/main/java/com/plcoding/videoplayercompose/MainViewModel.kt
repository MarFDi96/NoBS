package com.plcoding.videoplayercompose

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    val player: Player,
    private val metaDataReader: MetaDataReader
): ViewModel() {

    private val audioUris = savedStateHandle.getStateFlow("audioUris", emptyList<Uri>())

    val audioItems = audioUris.map { uris ->
        uris.map { uri ->
            AudioItem(
                contentUri = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())




   /* init {
        player.prepare()
    }*/

    fun addAudioUri(uri: Uri) {
        savedStateHandle["audioUris"] = audioUris.value + uri
        player.addMediaItem(MediaItem.fromUri(uri))
    }

    fun playAudio(uri: Uri) {
        /*player.setMediaItem(
            audioItems.value.find { it.contentUri == uri }?.mediaItem ?: return
        )*/
        val intent = Intent(context, MediaPlayerService::class.java)
        intent.action = "PLAY_AUDIO"
        intent.data = audioItems.value.find { it.contentUri == uri }?.contentUri ?: return
        context.startService(intent)
    }


}