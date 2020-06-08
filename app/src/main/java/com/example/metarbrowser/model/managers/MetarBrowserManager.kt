package com.example.metarbrowser.model.managers

import android.os.Bundle
import android.util.Log
import com.example.metarbrowser.model.interfaces.INetworkResponseCallback
import com.example.metarbrowser.utilities.Constants
import com.example.metarbrowser.utilities.MetarData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.get

class MetarBrowserManager: INetworkResponseCallback, KoinComponent {

    val TAG: String = javaClass.simpleName
    private val filteredStationList : MutableList<String> = mutableListOf()
    private val metarDataMap : MutableMap<String, MetarData> = mutableMapOf()
    private var responseCallbacks : MutableList<INetworkResponseCallback> = mutableListOf()

    private val downloadManager: DownloadManager = get()
    private val repositoryManager: RepositoryManager = get()

    init {
        Log.i(TAG, "Initialized")

        GlobalScope.launch {
            val dataList = repositoryManager.fetchCachedMetar()
            for (metarData in dataList) {
                metarDataMap.put(metarData.code, metarData)
            }
        }
        downloadManager.registerCallback(this)
        downloadManager.initialize()
    }

    fun registerCallback(callback : INetworkResponseCallback) {
        responseCallbacks.add(callback)
    }

    fun unregisterCallback(callback : INetworkResponseCallback) {
        if (responseCallbacks.contains(callback)) {
            responseCallbacks.remove(callback)
        }
    }

    fun fetchMetarData(stationCode : String) {
        if (stationCode.isNotEmpty()) {
            downloadManager.downloadMetarData(stationCode)
        }
    }

    fun getMetarDataMap() : MutableMap<String, MetarData> {
        return metarDataMap
    }

    fun notifyObservers(type: Int, bundle: Bundle) {
        for (callback in responseCallbacks) {
            callback.onDataFetchComplete(type, bundle)
        }
    }

    override fun onDataFetchComplete(dataType: Int, dataBundle: Bundle) {
        when(dataType) {
            Constants.TYPE_FETCH_METAR_DATA_DETAILS -> {
                val networkStatus = dataBundle.getInt(Constants.BUNDLE_KEY_STATUS)

                if (networkStatus == Constants.NETWORK_STATUS_DOWNLOAD_COMPLETED) {
                    val metarData: MetarData? = dataBundle.getParcelable(Constants.BUNDLE_KEY_DATA)
                    metarData?.let {metarData -> metarDataMap.put(metarData.code, metarData) }
                    metarData?.let { metarData ->  repositoryManager.insertMetar(metarData) }
                }
            }
        }
        notifyObservers(dataType, dataBundle)
    }
}
