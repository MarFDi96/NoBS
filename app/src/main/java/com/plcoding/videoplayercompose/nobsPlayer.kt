@file:OptIn(ExperimentalFoundationApi::class)

package com.plcoding.videoplayercompose

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.plcoding.videoplayercompose.ui.theme.AudioPlayerComposeTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun nobsPlayer (viewModel: MainViewModel){

    AudioPlayerComposeTheme {
        //val viewModel = hiltViewModel<MainViewModel>()
        var showPlaylistList by remember { mutableStateOf(false) }
        var selectedPlayListName by remember { mutableStateOf("") }
        val queuedTracks by viewModel.trackItems.collectAsState()
        var selectedItem by remember { mutableStateOf<AudioItem?>(null) }


        var lifecycle by rememberSaveable {
            mutableStateOf(Lifecycle.Event.ON_CREATE)
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                lifecycle = event
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }


        Column(
            modifier = Modifier
                //.fillMaxSize()
                .padding(16.dp)
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).also {
                        it.player = viewModel.player
                    }
                },
                update = {
                    when (lifecycle) {
                        Lifecycle.Event.ON_PAUSE -> {
                            //it.onPause()
                            //it.player?.pause()
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            //it.onResume()
                        }
                        else -> Unit
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {  showPlaylistList = true}) {
                    Icon(Icons.Filled.PlaylistPlay, contentDescription = "Playlists")
                }

                Text(
                    text = "Now playing",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )


            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(queuedTracks) { mediaItem ->
                    var isContextMenuVisible by remember { mutableStateOf(false) }
                    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                contextMenuOffset = coordinates.positionInRoot()
                            }
                    ){
                    Text(
                        text = mediaItem.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    viewModel.jumpToTrack(queuedTracks.indexOf(mediaItem))
                                },
                                onLongClick = {
                                    selectedItem = mediaItem
                                    isContextMenuVisible = true
                                    //viewModel.removeTrackFromPlaylist(mediaItem.contentUri, selectedPlayListName)
                                }
                            )
                            .padding(16.dp)
                    )
                    DropdownMenu(
                        expanded = isContextMenuVisible,
                        onDismissRequest = { isContextMenuVisible = false },
                        offset = DpOffset(contextMenuOffset.x.dp, contextMenuOffset.y.dp),
                        modifier = Modifier.background(if (isSystemInDarkTheme()) Color.DarkGray else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remove from playlist") },
                            onClick = {
                                viewModel.removeTrackFromPlaylist(
                                    mediaItem.contentUri,
                                    selectedPlayListName
                                )
                                isContextMenuVisible = false
                            }
                        )
                    }
                }
                }
            }
            if (showPlaylistList) {
                PlaylistList(
                    playlists = viewModel.playlists.collectAsState().value.keys.toList(),
                    onPlaylistClick = { playlistName ->
                        selectedPlayListName = playlistName
                        viewModel.playPlaylist(playlistName)
                        showPlaylistList = false
                    },
                    onPlaylistLongClick = { playlistName ->
                        selectedPlayListName = playlistName
                        viewModel.deletePlaylist(playlistName)
                        showPlaylistList = false
                    },
                    onClose = { showPlaylistList = false }
                )
            }
        }
    }
}

@Composable
fun PlaylistList(
    playlists: List<String>,
    onPlaylistLongClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onClose: () -> Unit
) {
    var showDeleteMenu by remember { mutableStateOf(false) }
    var selectedPlaylistToDelete by remember { mutableStateOf("") }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    AlertDialog(
        containerColor = ( if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
        onDismissRequest = onClose,
        title = { Text("Playlists") },
        text = {
            LazyColumn {
                items(playlists) { playlistName ->
                    Box(modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            contextMenuOffset = coordinates.positionInRoot()
                        }
                    ) {
                        Text(
                            text = playlistName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onPlaylistClick(playlistName) },
                                    onLongClick = {
                                        selectedPlaylistToDelete = playlistName
                                        showDeleteMenu = true
                                    }
                                )
                                .padding(16.dp)
                        )

                        DropdownMenu(
                            expanded = showDeleteMenu && selectedPlaylistToDelete == playlistName,
                            onDismissRequest = { showDeleteMenu = false },
                            offset = DpOffset(contextMenuOffset.x.dp, contextMenuOffset.y.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Playlist") },
                                onClick = {
                                    onPlaylistLongClick(selectedPlaylistToDelete)
                                    showDeleteMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}