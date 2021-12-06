package com.example.musicplayermyexample

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MyMusicService : MediaBrowserService(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    var link: String = ""
    private lateinit var musicStoppedListener: MusicStoppedListener

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not Yet ready")
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot {
        return BrowserRoot("BROWSER", null)
    }

    override fun onLoadChildren(p0: String, p1: Result<MutableList<MediaBrowser.MediaItem>>) {

    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()

        mediaPlayer.apply {
            setOnSeekCompleteListener(this@MyMusicService)
            setOnPreparedListener(this@MyMusicService)
            setOnErrorListener(this@MyMusicService)
            setOnSeekCompleteListener(this@MyMusicService)
            setOnInfoListener(this@MyMusicService)
            setOnBufferingUpdateListener(this@MyMusicService)
        }
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
     //   notificationManager.setPlayer(mediaPlayer as Player)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        link = intent?.getStringExtra("AUDIO").toString()
        Log.d("CHECK__", "ONSTARTCOOMMAND")
        musicStoppedListener = App.context as MusicStoppedListener
        mediaPlayer.reset()
        if (!mediaPlayer.isPlaying) {
            try {
                mediaPlayer.setDataSource(link)
                mediaPlayer.prepareAsync()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
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

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.let {
            if (it.isPlaying) {
                mp.stop()
            }
        }
        stopSelf()
    }

    override fun onPrepared(p0: MediaPlayer?) {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
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