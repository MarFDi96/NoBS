package com.plcoding.videoplayercompose

import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.plcoding.videoplayercompose.ui.theme.AudioPlayerComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var controller: MediaController
    private val REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioPlayerComposeTheme {

                BottomNavBar()

            }
        }
    }
    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MediaPlayerService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                },
            MoreExecutors.directExecutor()
        )
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Required to access audio files")
                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1) })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.READ_MEDIA_AUDIO)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Required to access audio files")
                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 1) })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 1)
            }
        } else {
            // Permission granted
            Log.d("AudioPlayer", "Permission granted")
        }
    }
}


@Composable
fun BottomNavBar() {
    //val navigationController = rememberNavController()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val context = LocalContext.current.applicationContext
    val viewModel = hiltViewModel<MainViewModel>()
    val selected = remember {mutableStateOf(Icons.Default.Home)}

    //reminder; usar un switch o una variable o algo que haga el selected.value
    // cambie con el pagerstate o lo que sea del horizontal pager

    Scaffold(
        bottomBar = {
            BottomAppBar(
                contentColor = Color.White
            ) {
                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Audiotrack
//                        navigationController.navigate(Screens.Home.screen) {
//                            popUpTo(0)
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(0) // Navigate to page 0 (Home)
                        }

                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Player",
                        tint = //if (selected.value == Icons.Default.Audiotrack) Color.White else Color.DarkGray
                        if (pagerState.currentPage == 0) Color.White else Color.DarkGray
                    )
                }

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Dashboard
//                        navigationController.navigate(Screens.Playlists.screen) {
//                            popUpTo(0)
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(1) // Navigate to page 0 (Home)
                        }

                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Music Library",
                        tint = //if (selected.value == Icons.Default.Dashboard) Color.White else Color.DarkGray
                        if (pagerState.currentPage == 1) Color.White else Color.DarkGray
                    )

                }

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Equalizer
//                        navigationController.navigate(Screens.Equalizer.screen) {
//                            popUpTo(0)
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(2) // Navigate to page 0 (Home)
                        }

                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = //if (selected.value == Icons.Default.Equalizer) Color.White else Color.DarkGray
                        if (pagerState.currentPage == 2) Color.White else Color.DarkGray
                    )

                }

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Settings
                        //navigationController.navigate(Screens.Options.screen) {
                        //    popUpTo(0)
                        //}
                        scope.launch {
                            pagerState.animateScrollToPage(3) // Navigate to page 0 (Home)
                        }

                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Options",
                        tint = //if (selected.value == Icons.Default.Settings) Color.White else Color.DarkGray
                        if (pagerState.currentPage == 3) Color.White else Color.DarkGray
                    )

                }

            }
        }
    ) { paddingValues ->
        HorizontalPager(
            count = 4, // Number of screens
            state = pagerState,
            modifier = Modifier.padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> Home(viewModel)
                1 -> Playlists(pagerState, viewModel, scope)
                2 -> Equalizer()
                3 -> Options()
            }
        }
//        NavHost(
//            navController = navigationController,
//            startDestination = Screens.Playlists.screen,
//            modifier = Modifier.padding(paddingValues)) {
//            composable(Screens.Home.screen) { Home(viewModel) }
//            composable(Screens.Playlists.screen) { Playlists(navigationController, viewModel) }
//            composable(Screens.Equalizer.screen) {Equalizer() }
//            composable(Screens.Options.screen) { Options() }
//        }
    }
}

