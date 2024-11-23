package com.plcoding.videoplayercompose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
//TODO: re-ver la parte de playaudio del viewmodel y que se latchee bien al servicio y el servicio se encargue del playback para que no se muera el thread
@AndroidEntryPoint
class MediaPlayerService : MediaLibraryService() {

    @Inject
    lateinit var player : Player
    lateinit var session : MediaLibrarySession
    private val CHANNEL_ID = "mediaPlayerChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        session = MediaLibrarySession.Builder(this, player,
            object: MediaLibrarySession.Callback {
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> {

                    val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
                    return Futures.immediateFuture(updatedMediaItems)
                }
            }).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY_AUDIO" -> {
                val uri = intent.data!! // Get the URI from the intent
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                startForeground(NOTIFICATION_ID, createNotification(), FOREGROUND_SERVICE_TYPE_LOCATION)
            }

            else -> {
                super.onStartCommand(intent, flags, startId)
            }
        }
        return START_STICKY
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return session
    }

    override fun onDestroy() {
        session.release()
        player.release()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Media Player")
            .setContentText("Playing audio")
            .setSmallIcon(androidx.media3.exoplayer.dash.R.drawable.notification_template_icon_low_bg) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    // Create the notification channel (for Android Oreo and above)
    private fun createNotificationChannel() {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Player Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }
}