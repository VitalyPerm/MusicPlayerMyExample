package com.example.musicplayermyexample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MyMusicService : MediaBrowserService(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {
    private lateinit var mediaPlayer: SimpleExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var notificationManager: PlayerNotificationManager
    var link: String = "https://firebasestorage.googleapis.com/v0/b/spotifybrother-85d95.appspot.com/o/Nirvana%20-%20Dumb%20.mp3?alt=media&token=c9bbbd35-b396-4d1a-822e-a72fce597962"
    private lateinit var musicStoppedListener: MusicStoppedListener

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not Yet ready")
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot {
        return BrowserRoot("BROWSER", null)
    }

    override fun onLoadChildren(p0: String, p1: Result<MutableList<MediaBrowser.MediaItem>>) {

    }

    private fun buildMediaSource(videoUrl: String): HlsMediaSource? {
        val uri = Uri.parse(videoUrl)
        // Create a HLS media source pointing to a playlist uri.
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mediaPlayer = SimpleExoPlayer.Builder(this).build()
        dataSourceFactory = DefaultHttpDataSourceFactory(Util.getUserAgent(this, "PLAYER"))
        val extractorsFactory = DefaultExtractorsFactory()
        val mediaSource = ExtractorMediaSource(Uri.parse(link), dataSourceFactory, extractorsFactory, null, null)
        mediaPlayer.prepare(mediaSource)
        mediaPlayer.playWhenReady = true

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this, "CHANNEL_ID", R.string.channel_name, R.string.channel_desc, 123,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return "TITLE"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val i = Intent(this@MyMusicService, MainActivity::class.java)
                    return PendingIntent.getActivity(this@MyMusicService, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return "TEXT"
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                    return null
                }
            }
        )
         notificationManager.setPlayer(mediaPlayer)

//        val notification = NotificationCompat.Builder(this, "123").build()
//        notification.category = Notification.CATEGORY_SERVICE
//        startForeground(1, notification)
        startForeground()
    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("CHECK__", "ONSTARTCOOMMAND")
//        musicStoppedListener = App.context as MusicStoppedListener
//        mediaPlayer.reset()
//        if (!mediaPlayer.isPlaying) {
//            try {
//                mediaPlayer.setDataSource(link)
//                mediaPlayer.prepareAsync()
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.let {
            if (it.isPlaying) {
                mp.stop()
            }
        }
        stopSelf()
    }

    override fun onPrepared(p0: MediaPlayer?) {
//        if (!mediaPlayer.isPlaying) {
//            mediaPlayer.start()
//        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        when (p1) {
            MediaPlayer.MEDIA_ERROR_IO -> Toast.makeText(this, "MEDIA_ERROR_IO", Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Toast.makeText(this, "MEDIA_ERROR_SERVER_DIED", Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Toast.makeText(this, "MEDIA_ERROR_UNKNOWN", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onSeekComplete(p0: MediaPlayer?) {

    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return true
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {

    }
}