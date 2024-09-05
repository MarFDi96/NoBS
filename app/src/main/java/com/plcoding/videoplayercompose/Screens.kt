package com.plcoding.videoplayercompose

sealed class Screens (val screen: String){

        data object Home: Screens("home")
        data object Playlists: Screens("playlists")
        data object Equalizer: Screens("equalizer")
        data object Options: Screens("options")

}