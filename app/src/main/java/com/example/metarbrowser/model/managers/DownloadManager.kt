package com.example.metarbrowser.model.managers

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import com.example.metarbrowser.MetarBrowserApp
import com.example.metarbrowser.model.interfaces.INetworkChangeListener
import com.example.metarbrowser.model.interfaces.INetworkResponseCallback
import com.example.metarbrowser.utilities.Constants.BUNDLE_KEY_DATA
import com.example.metarbrowser.utilities.Constants.BUNDLE_KEY_STATUS
import com.example.metarbrowser.utilities.Constants.METAR_LABEL_RAW_DATA
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_DOWNLOAD_COMPLETED
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_DOWNLOAD_STARTED
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_ERROR_FILE_NOT_FOUND
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_ERROR_NO_INTERNET
import com.example.metarbrowser.utilities.Constants.TYPE_FETCH_METAR_DATA_DETAILS
import com.example.metarbrowser.utilities.MetarData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class DownloadManager : KoinComponent, INetworkChangeListener {

    private val TAG : String = javaClass.simpleName
    private var responseCallbacks : MutableList<INetworkResponseCallback> = mutableListOf()
    private val metarBrowserManager: MetarBrowserManager by inject()
    private val context: Context by inject()

    fun initialize() {
        NetworkManager.registerNetworkChangeReceiver()
        NetworkManager.registerChangeListener(this)
        if (isNetworkConnected()) {
            startDownloadScheduler()
        }
    }

    fun registerCallback(callback : INetworkResponseCallback) {
        responseCallbacks.add(callback)
    }

    fun unregisterCallback(callback : INetworkResponseCallback) {
        if (responseCallbacks.contains(callback)) {
            responseCallbacks.remove(callback)
        }
    }

    private fun notifyDownloadStatus(type: Int, bundle: Bundle) {
        for (callback in responseCallbacks) {
            callback.onDataFetchComplete(type, bundle)
        }
    }

    @SuppressLint("CheckResult")
    fun downloadMetarData(stationCode: String) {

        notifyDownloadStatus(TYPE_FETCH_METAR_DATA_DETAILS, createProgressBundle(stationCode))

        getObservableForStationCode(stationCode)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe (
            {  metarData ->

                Log.i(TAG, "Result from server for ${metarData.code} = ${metarData.decodedData}")
                notifyDownloadStatus(TYPE_FETCH_METAR_DATA_DETAILS, createDataBundle(metarData))
            },
            { error ->
                when(error) {
                    is FileNotFoundException -> {
                        Log.i(TAG, "Station not found")
                        notifyDownloadStatus(TYPE_FETCH_METAR_DATA_DETAILS, createErrorBundle(
                            NETWORK_STATUS_ERROR_FILE_NOT_FOUND, stationCode))
                    }
                    else -> {
                        Log.i(TAG, "No internet connectivity")
                        notifyDownloadStatus(TYPE_FETCH_METAR_DATA_DETAILS, createErrorBundle(
                            NETWORK_STATUS_ERROR_NO_INTERNET, stationCode))
                    }

                }
            },
            { Log.i(TAG, "Download complete")} )
    }

    private fun getObservableForStationCode(stationCode: String ) = Observable.create<MetarData> { emitter ->
        if (isNetworkConnected()) {
            try {
                val inputStream =
                    URL("https://tgftp.nws.noaa.gov/data/observations/metar/decoded/$stationCode.TXT").openStream()

                if (inputStream != null) {
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val metarData = getMetarData(bufferedReader, stationCode)
                    emitter.onNext(metarData)
                    emitter.onComplete()
                }
            } catch (e : Exception) {
                Log.e(TAG, "Error while downloading", e)
                emitter.onError(e)
            }
        } else {
            emitter.onError(Exception("No internet connectivity"))
        }

    }

    private fun getMetarData(bufferedReader: BufferedReader, stationCode: String) : MetarData {
        val metarData = MetarData()
        val stringBuilder = StringBuilder();
        var lineNo = 1

        metarData.code = stationCode

        bufferedReader.forEachLine { line ->

            stringBuilder.append(line).append('\n')

            if (lineNo == 1) {
                if (line.indexOf('(') != -1) {
                    val stationName: String = line.substring(0, line.indexOf('('))
                    metarData.stationName = stationName
                } else {
                    metarData.stationName = "Station Name not Available"
                }
            } else if (lineNo == 2) {
                metarData.lastUpdateTime = line
            }

            if (line.startsWith(METAR_LABEL_RAW_DATA)) {
                val rawData: String = line.substring(METAR_LABEL_RAW_DATA.length + 1)
                metarData.rawData = rawData
            }
            lineNo++
        }

        metarData.decodedData = stringBuilder.toString()
        return metarData
    }

    private fun isNetworkConnected() : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun createDataBundle(data: MetarData) : Bundle {
        return Bundle().apply {
            putInt(BUNDLE_KEY_STATUS, NETWORK_STATUS_DOWNLOAD_COMPLETED)
            putParcelable(BUNDLE_KEY_DATA, data)
        }
    }

    private fun createProgressBundle(stationCode: String) : Bundle {
        return Bundle().apply {
            putInt(BUNDLE_KEY_STATUS, NETWORK_STATUS_DOWNLOAD_STARTED)
            putParcelable(BUNDLE_KEY_DATA, MetarData(code = stationCode))
        }
    }

    private fun createErrorBundle(status: Int, stationCode: String) : Bundle{
        return Bundle().apply {
            putInt(BUNDLE_KEY_STATUS, status)
            putParcelable(BUNDLE_KEY_DATA, MetarData(code = stationCode))
        }
    }

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private lateinit var schedulerHandler: ScheduledFuture<*>
    //TODO: Create a ThreadPoolExecutor
    // TODO: CoroutineScope {  withContext(threadPool.asCoroutineDispatcher()) {} }

    private fun startDownloadScheduler() {
        Log.i(TAG, "Download scheduler started")

        schedulerHandler = scheduler.scheduleAtFixedRate({

                CoroutineScope(Dispatchers.Default).launch {

                    val stationList1: Set<String> = metarBrowserManager.getMetarDataMap().keys

                    stationList1.forEach { value ->
                        launch {
                            Log.i(TAG, "Child launch started for $value")
                            downloadDetails(value)
                            // run something on the main thread
                        }
                    }
                }
            },
            1,
            10,
            TimeUnit.SECONDS
        )
    }

    fun stopDownloadScheduler() {
        if (!schedulerHandler.isCancelled) {
            schedulerHandler.cancel(true)
        }
    }

    private fun downloadDetails(stationCode: String) {
        try {
            Log.i(TAG, "Download started for $stationCode")
            if (isNetworkConnected()) {
                val inputStream =
                    URL("https://tgftp.nws.noaa.gov/data/observations/metar/decoded/$stationCode.TXT").openStream()

                if (inputStream != null) {
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val metarData = getMetarData(bufferedReader, stationCode)
                    Log.i(
                        TAG,
                        "Metar data downloaded for ${metarData.code}\n${metarData.decodedData}\n*********************************************************"
                    )
                }
            } else {
                Log.i(TAG, "No internet connectivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while downloading", e)

        }
    }

    override fun onConnected() {
        startDownloadScheduler()
    }

    override fun onDisconnected() {
        stopDownloadScheduler()
    }
}