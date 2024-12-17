package com.plcoding.videoplayercompose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@UnstableApi @AndroidEntryPoint
class MediaPlayerService : MediaSessionService(), MediaSession.Callback {

    @Inject
    lateinit var player : Player
    private val CHANNEL_ID = "mediaPlayerChannel"
    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).setCallback(this).build()
        createNotification(mediaSession!!)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY_AUDIO" -> {
                val uri = intent.data!! // Get URI from the intent
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.addListener(object : Player.Listener {
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        equalizer = Equalizer(0, audioSessionId)
                        // ... configure equalizer bands, frequencies, gains, etc. ...
                        equalizer?.enabled = true
                    }
                })
                player.play()

            }

            "JUMP_TO_TRACK" -> {
                val trackIndex = intent.getIntExtra("trackIndex", -1)
                if (trackIndex != -1) {
                    // Perform the jump using ExoPlayer:
                    player.seekTo(trackIndex, 0L)
                }
            }

            else -> {
                super.onStartCommand(intent, flags, startId)
            }
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
        return MediaSession.ConnectionResult.accept(
            availableSessionCommands.build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onDestroy() {
        player.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }
    override fun onUpdateNotification(session: MediaSession) {
        createNotification(session) //calling method where we create notification
    }
    fun  createNotification(session: MediaSession) {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID,"Channel", NotificationManager.IMPORTANCE_LOW))


        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.test)
            .setContentTitle("NoBsPlayer")
            .setContentText("Just music, no bs")
            .setColorized(true)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
            .build()
        notificationManager.notify(1,notificationCompat)
    }


}