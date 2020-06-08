package com.example.metarbrowser

import com.example.metarbrowser.model.managers.DownloadManager
import com.example.metarbrowser.model.managers.MetarBrowserManager
import com.example.metarbrowser.model.managers.RepositoryManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {
    single(name = "metarBrowserManager", createOnStart = true) { MetarBrowserManager() }
    single { DownloadManager() }
    single { RepositoryManager() }
}