package com.example.metarbrowser

import android.app.Application
import com.example.metarbrowser.model.managers.MetarBrowserManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.startKoin
import org.koin.standalone.KoinComponent

class MetarBrowserApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }
}