package com.example.metarbrowser

import android.content.Context
import android.content.SharedPreferences
import com.example.metarbrowser.model.managers.DownloadManager
import com.example.metarbrowser.model.managers.MetarBrowserManager
import com.example.metarbrowser.model.managers.RepositoryManager
import com.example.metarbrowser.utilities.Constants.SHARED_PREF_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {
    single(name = "metarBrowserManager", createOnStart = true) { MetarBrowserManager() }
    single { DownloadManager() }
    single { RepositoryManager() }
    single<SharedPreferences> { androidContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)}
    single<SharedPreferences.Editor> { androidContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()}
}