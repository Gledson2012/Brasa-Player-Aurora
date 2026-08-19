package com.example

import android.app.Application
import com.example.di.ServiceLocator

class MusicPlayerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
