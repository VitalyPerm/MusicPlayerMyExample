package com.example.musicplayermyexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity(), MusicStoppedListener {
    private lateinit var play: View
    private lateinit var back: View
    private lateinit var tvTime: TextView
    private var musicPlaying = false
    private var time: Long = 0L

    private val audioLink =
        "https://firebasestorage.googleapis.com/v0/b/spotifybrother-85d95.appspot.com/o/Nirvana%20-%20Dumb%20.mp3?alt=media&token=c9bbbd35-b396-4d1a-822e-a72fce597962"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTime = findViewById(R.id.tv_time)
        App.context = MainActivity()
        play = findViewById(R.id.play)
        back = findViewById(R.id.back)
        back.setOnClickListener { skip10Sec() }
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
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter("my_message"))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val text = p1?.getLongExtra("SEEK", 0L).toString()
            tvTime.text = "0:$text"
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun skip10Sec() {
        val intent = Intent(this, MyMusicService::class.java)
        intent.addCategory("SKIP_TEN")
        startForegroundService(intent)
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