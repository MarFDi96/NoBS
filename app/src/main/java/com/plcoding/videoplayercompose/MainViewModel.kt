package com.plcoding.videoplayercompose

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@UnstableApi @HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    val player: Player,
    private val metaDataReader: MetaDataReader,
    private val dataStore: DataStore<Preferences>
): ViewModel() {

    private val contentResolver = context.contentResolver!!
    private val audioUris = savedStateHandle.getStateFlow("audioUris", emptyList<Uri>())
    private val trackUris = savedStateHandle.getStateFlow("trackUris", emptyList<Uri>())
    private val _playlists = MutableStateFlow<Map<String, List<Uri>>>(emptyMap())
    val playlists: StateFlow<Map<String, List<Uri>>> = _playlists.asStateFlow()

    val trackItems = trackUris.map { uris ->
        uris.map { uri ->
            AudioItem(
                contentUri = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val audioItems = audioUris.map { uris ->
        uris.map { uri ->
            AudioItem(
                contentUri = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    /*val trackItems = trackUris.map { uris ->
        uris.map { uri ->
            AudioItem(
                contentUri = uri,
                //mediaItem = MediaItem.fromUri(uri),
                mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(metaDataReader.getMetaDataFromUri(uri)?.title)
                            .setArtist(metaDataReader.getMetaDataFromUri(uri)?.artist)
                            .setAlbumTitle(metaDataReader.getMetaDataFromUri(uri)?.album)
                            .setArtworkUri(metaDataReader.getMetaDataFromUri(uri)?.albumArt?.let { Uri.parse(it.toString()) })
                            .build()
                    )
                    .build(),
                name = metaDataReader.getMetaDataFromUri(uri)?.title ?: metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val audioItems = audioUris.map { uris ->
        uris.map { uri ->
            AudioItem(
                contentUri = uri,
                //mediaItem = MediaItem.fromUri(uri),
                mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(metaDataReader.getMetaDataFromUri(uri)?.title)
                            .setArtist(metaDataReader.getMetaDataFromUri(uri)?.artist)
                            .setAlbumTitle(metaDataReader.getMetaDataFromUri(uri)?.album)
                            .setArtworkUri(metaDataReader.getMetaDataFromUri(uri)?.albumArt?.let { Uri.parse(it.toString()) })
                            .build()
                    )
                    .build(),
                name = metaDataReader.getMetaDataFromUri(uri)?.title ?: metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())*/

    init {
            scanForAudioFiles()
        viewModelScope.launch {
            loadPlaylists()
        }
    }

    fun addAudioUri(uri: Uri) {
        savedStateHandle["audioUris"] = audioUris.value + uri
        //player.addMediaItem(MediaItem.fromUri(uri))
    }

    fun addTrackToPlaylist(uri: Uri) {
        player.addMediaItem(MediaItem.fromUri(uri))
        savedStateHandle["trackUris"] = trackUris.value + uri
    }
    fun clearTrackUris() {
        savedStateHandle["trackUris"] = emptyList<Uri>()
    }

    fun clearAudioUris(){
        savedStateHandle["audioUris"] = emptyList<Uri>()
    }

    fun playAudio(uri: Uri) {
        val intent = Intent(context, MediaPlayerService::class.java)
        intent.action = "PLAY_AUDIO"
        intent.data = audioItems.value.find { it.contentUri == uri }?.contentUri ?: return
        context.startService(intent)

        savedStateHandle["trackUris"] = trackUris.value + uri
    }

    fun scanForAudioFiles() {
        clearAudioUris()
        Log.d("AudioFile", "Scanning for audio files...")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA // Path to file
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val data = cursor.getString(dataColumn) // File path
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                addAudioUri(contentUri)
                // Do something with the audio file information (title, contentUri, etc.)
                Log.d("AudioFile", "Title: $title, URI: $contentUri, Path: $data")
            }
        }
    }

    private suspend fun loadPlaylists() {
        // Read playlists from DataStore
        dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Deserialize playlist data from preferences
            val playlistMap = mutableMapOf<String, List<Uri>>()
            for (playlistName in preferences.asMap().keys) {
                val trackUris = preferences[stringSetPreferencesKey(playlistName.toString())]
                    ?.map { Uri.parse(it) } ?: emptyList()
                playlistMap[playlistName.toString()] = trackUris
            }
            playlistMap
        }.collect { playlistMap ->
            _playlists.value = playlistMap
        }
    }

    fun createPlaylist(playlistName: String) {
        if (playlistName.isNotBlank() && !playlists.value.containsKey(playlistName)) {
            _playlists.update { it + (playlistName to emptyList()) }
        }
        viewModelScope.launch {
            savePlaylists() // Save playlists after creation
        }
    }
    fun deletePlaylist(playlistName: String) {
        _playlists.update { it - playlistName }
        viewModelScope.launch {
            savePlaylists()
        }
    }

    fun addTrackToPlaylist(trackUri: Uri, playlistName: String) {
        _playlists.update { playlists ->
            val currentTracks = playlists[playlistName] ?: emptyList()
            if (!currentTracks.contains(trackUri)) {
                playlists + (playlistName to (currentTracks + trackUri))
            } else {
                playlists // Track already in playlist, do nothing
            }
        }
        viewModelScope.launch {
            savePlaylists() // Save playlists after adding a track
        }
    }

    fun removeTrackFromPlaylist(trackUri: Uri, playlistName: String) {
        _playlists.update { playlists ->
            val currentTracks = playlists[playlistName] ?: emptyList()
            playlists + (playlistName to currentTracks.minus(trackUri))
        }
        viewModelScope.launch {
            savePlaylists() // Save playlists after removing a track
        }
    }

    private suspend fun savePlaylists() {
        // Write playlists to DataStore
        dataStore.edit { preferences ->
            preferences.clear() // Clear previous data
            for ((playlistName, trackUris) in playlists.value) {
                // Store playlist names as keys and track URIs as a string set
                preferences[stringSetPreferencesKey(playlistName)] =
                    trackUris.map { it.toString() }.toSet()
            }
        }
    }

    fun playPlaylist(playlistName: String) {
        viewModelScope.launch {
            val trackUris = playlists.value[playlistName] ?: emptyList()
            clearTrackUris()
            player.clearMediaItems()

            trackUris.forEach { uri ->
                addTrackToPlaylist(uri)
            }

            if (trackUris.isNotEmpty()) {
                jumpToTrack(0)
            }
        }
    }

    fun jumpToTrack(trackIndex: Int) {
        Log.d("TRACK TRACKER", "Jumping to track: $trackIndex")
        val intent = Intent(context, MediaPlayerService::class.java)
        intent.action = "JUMP_TO_TRACK"
        intent.putExtra("trackIndex", trackIndex) // Pass the track index as an extra
        context.startService(intent)
    }


}