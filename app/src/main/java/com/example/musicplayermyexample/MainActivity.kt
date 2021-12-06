package com.example.musicplayermyexample

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity(), MusicStoppedListener {
    private lateinit var play: Button
    private var musicPlaying = false

    private val audioLink = "https://firebasestorage.googleapis.com/v0/b/spotifybrother-85d95.appspot.com/o/Nirvana%20-%20Dumb%20.mp3?alt=media&token=c9bbbd35-b396-4d1a-822e-a72fce597962"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.context = MainActivity()
        play = findViewById(R.id.play)
        play.setOnClickListener {
            if (!musicPlaying) {
                playAudio()
                musicPlaying = true
                play.text = "Stop"
            } else {
                stopAudio()
                musicPlaying = false
                play.text = "Play"
            }
        }
    }

    private fun playAudio() {
        val intent = Intent(this, MyMusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
    }

    private fun stopAudio() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopService(Intent(this, MyMusicService::class.java))
            }
        }catch (e: SecurityException){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMusicStopped() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
    }
}