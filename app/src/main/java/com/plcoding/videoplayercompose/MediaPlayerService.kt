package com.plcoding.videoplayercompose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
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
//TODO: re-ver la parte de playaudio del viewmodel y que se latchee bien al servicio y el servicio se encargue del playback para que no se muera el thread
@UnstableApi @AndroidEntryPoint
class MediaPlayerService : MediaSessionService(), MediaSession.Callback {

    @Inject
    lateinit var player : Player
    private val CHANNEL_ID = "mediaPlayerChannel"
    private val NOTIFICATION_ID = 1
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
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
                player.play()


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

        // NotificationCompat here.
        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            // Text can be set here
            // but I believe setting MediaMetaData to MediaSession would be enough.
            // I havent tested it deeply yet but did display artist from session
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("your Content title")
            .setContentText("your content text")
            // set session here
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
            .build()
        notificationManager.notify(1,notificationCompat)
    }


}