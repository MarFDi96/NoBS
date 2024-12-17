package com.plcoding.videoplayercompose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Playlists(navigationController: NavController, viewModel: MainViewModel) {
    //val viewModel = hiltViewModel<MainViewModel>()
    val songItems by viewModel.audioItems.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<AudioItem?>(null) }
    var searchText by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }


        Scaffold(
            topBar = {
                if (showSearchBar) {
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChanged = { newText -> searchText = newText },
                        onCloseClicked = { showSearchBar = false }
                    )
                } else {
                    SmallTopAppBar(
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray,
                            actionIconContentColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                        ),
                        title = { Text("Music Library") },
                        actions = {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search"
                                )
                            }
                            IconButton(onClick = { viewModel.scanForAudioFiles() }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Scan for audio files"
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                val filteredItems = if (searchText.isBlank()) {
                    songItems
                } else {
                    songItems.filter { it.name.contains(searchText, ignoreCase = true) }
                }

                items(filteredItems) { item ->
                    var isContextMenuVisible by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                contextMenuOffset = coordinates.positionInRoot()
                            }
                    ){
                    Text(
                        text = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    viewModel.clearTrackUris()
                                    viewModel.playAudio(item.contentUri)
                                    navigationController.navigate(Screens.Home.screen) {
                                        popUpTo(0)
                                    }
                                },
                                onLongClick = {
                                    selectedItem = item
                                    isContextMenuVisible = true
                                }
                            )
                            .padding(16.dp)
                    )
                    DropdownMenu(
                        expanded = isContextMenuVisible,
                        onDismissRequest = { isContextMenuVisible = false },
                        offset = DpOffset(contextMenuOffset.x.dp, contextMenuOffset.y.dp),
                        modifier = Modifier.background( if (isSystemInDarkTheme()) Color.DarkGray else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to Now Playing") },
                            onClick = {
                                viewModel.addTrackToPlaylist(item.contentUri)
                                isContextMenuVisible = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                // Todo; try to get this done
                                isContextMenuVisible = false
                            }
                        )
                    }
                }
                }
            }
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}