package com.example.playclone

import android.app.Application
import com.example.playclone.di.AppContainer

class PlayCloneApp : Application() {
    
    lateinit var container: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
