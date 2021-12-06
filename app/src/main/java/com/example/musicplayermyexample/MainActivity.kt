package com.example.musicplayermyexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity(), MusicStoppedListener {
    private lateinit var play: View
    private lateinit var back: View
    private lateinit var forward: View
    private lateinit var tvTime: TextView
    private lateinit var tvDuration: TextView
    private var musicPlaying = false
    private var time: Long = 0L
    private lateinit var broadcastReceiver: LocalBroadcastManager

    private val audioLink =
        "https://firebasestorage.googleapis.com/v0/b/spotifybrother-85d95.appspot.com/o/Nirvana%20-%20Dumb%20.mp3?alt=media&token=c9bbbd35-b396-4d1a-822e-a72fce597962"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTime = findViewById(R.id.tv_time)
        forward = findViewById(R.id.next)
        tvDuration = findViewById(R.id.tv_duration)
        App.context = MainActivity()
        play = findViewById(R.id.play)
        back = findViewById(R.id.back)
        back.setOnClickListener { skip10Sec(false) }
        forward.setOnClickListener { skip10Sec(true)
        Log.d("CHECK__", "forward clicked")}
        play.setOnClickListener {
            if (!musicPlaying) {
                playAudio()
                musicPlaying = true

            } else {
                stopAudio()
                musicPlaying = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        broadcastReceiver = LocalBroadcastManager.getInstance(this)
        broadcastReceiver.registerReceiver(receiver, IntentFilter("my_message"))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val text = p1?.getLongExtra("SEEK", 0L).toString()
            tvTime.text = "0:$text"
            val duration = p1?.getLongExtra("duration", 0L)
            tvDuration.text = convertLong(duration)
        }
    }

    private fun convertLong(l: Long?): String {
        val minutes = l?.rem(60)
        val hours = (l?.div(60)).toString().substringBefore('.')
        return "$hours:$minutes"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun skip10Sec(forward: Boolean) {
        val i = Intent("my_message")
        if (forward) i.putExtra("ten_forward", "ten_forward")
        else i.putExtra("ten_back", "ten_back")
        broadcastReceiver.sendBroadcast(i)
    }

    private fun playAudio() {
        val intent = Intent(this, MyMusicService::class.java).apply {
            putExtra("AUDIO", "dumb")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
    }

    private fun stopAudio() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopService(Intent(this, MyMusicService::class.java))
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMusicStopped() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
    }
}