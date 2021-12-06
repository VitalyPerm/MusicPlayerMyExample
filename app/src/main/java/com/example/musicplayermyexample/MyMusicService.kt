package com.example.musicplayermyexample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.media.browse.MediaBrowser
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.service.media.MediaBrowserService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MyMusicService : MediaBrowserService() {
    private lateinit var mediaPlayer: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private lateinit var broadcastReceiver: LocalBroadcastManager


    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not Yet ready")
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot {
        return BrowserRoot("BROWSER", null)
    }

    override fun onLoadChildren(p0: String, p1: Result<MutableList<MediaBrowser.MediaItem>>) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mediaPlayer = ExoPlayer.Builder(this).build()
        playSong(Links.dumb)

        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return "TITLE"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val i = Intent(this@MyMusicService, MainActivity::class.java)
                return PendingIntent.getActivity(
                    this@MyMusicService,
                    0,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return "TEXT"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return null
            }
        }
        notificationManager = PlayerNotificationManager.Builder(this, 2, "")
            .setMediaDescriptionAdapter(mediaDescriptionAdapter)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {})
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.channel_name)
            .build().apply { setPlayer(mediaPlayer) }
        startForeground()
        observeSeek(true)

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            val back = p1?.getStringExtra("ten_back")
            val forward = p1?.getStringExtra("ten_forward")
            Log.d("RECIVER", "back $back forward $forward")
            when {
                (p1?.getStringExtra("ten_back") == "ten_back") -> {
                    seekTo(false)
                }
                (p1?.getStringExtra("ten_forward") == "ten_forward") -> {
                    seekTo(true)
                }
            }
        }

    }

    fun observeSeek(observe: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            while (observe) {
                val time = mediaPlayer.currentPosition / 1000
                sendSeekBar(time)
                Log.d("CHECK_I", "$time")
                delay(1000)
            }
        }
    }

    fun sendSeekBar(time: Long) {
        val i = Intent("my_message")
        i.putExtra("SEEK", time)
        i.putExtra("duration", mediaPlayer.duration / 1000)
        broadcastReceiver.sendBroadcast(i)
    }


    fun playSong(url: String) {
        mediaPlayer.apply {
            addMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            //   sendDuration(mediaPlayer.duration)
        }
    }

    fun seekTo(forward: Boolean) {
        Log.d("CHECK___", "SEEKTO ${forward}")
        if (forward) {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + 10000)
        } else {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - 10000)
        }
    }


    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else ""
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
        broadcastReceiver = LocalBroadcastManager.getInstance(this)
        broadcastReceiver.registerReceiver(receiver, IntentFilter("my_message"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.hasCategory("SKIP_TEN")) {
                seekTo(false)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}