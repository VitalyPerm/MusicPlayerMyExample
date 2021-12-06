package com.example.musicplayermyexample

import android.app.Application
import android.content.Context

class App: Application() {

    override fun onCreate() {
        super.onCreate()
    }
    companion object{
        var context: Context? = null
    }
}