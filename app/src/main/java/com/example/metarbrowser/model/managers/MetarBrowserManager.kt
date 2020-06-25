package com.example.metarbrowser.model.managers

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.metarbrowser.model.interfaces.INetworkResponseCallback
import com.example.metarbrowser.utilities.Constants
import com.example.metarbrowser.utilities.Constants.BUNDLE_KEY_DATA
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_DOWNLOADING_ERROR
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_DOWNLOAD_COMPLETED
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_DOWNLOAD_STARTED
import com.example.metarbrowser.utilities.Constants.NETWORK_STATUS_ERROR_NO_INTERNET
import com.example.metarbrowser.utilities.Constants.SHARED_PREF_KEY_FILTERED_LIST_STATUS
import com.example.metarbrowser.utilities.Constants.STATUS_DOWNLOAD_COMPLETE
import com.example.metarbrowser.utilities.Constants.STATUS_DOWNLOAD_STARTED
import com.example.metarbrowser.utilities.Constants.STATUS_NOT_AVAILABLE
import com.example.metarbrowser.utilities.MetarData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import kotlin.jvm.internal.MagicApiIntrinsics

class MetarBrowserManager: INetworkResponseCallback, KoinComponent {

    val TAG: String = javaClass.simpleName
    private var filteredStationList : MutableList<String>? = null
    private val metarDataMap : MutableMap<String, MetarData> = mutableMapOf()
    private var responseCallbacks : MutableList<INetworkResponseCallback> = mutableListOf()
    private var filteredListMutableLiveData: MutableLiveData<MutableList<String>>? = null

    private val downloadManager: DownloadManager = get()
    private val repositoryManager: RepositoryManager = get()
    private val sharedPrefEditor: SharedPreferences.Editor = get()

    init {
        Log.i(TAG, "Initialized")

        GlobalScope.launch {
            val dataList = repositoryManager.fetchCachedMetar()
            for (metarData in dataList) {
                metarDataMap.put(metarData.code, metarData)
            }
            filteredStationList = repositoryManager.fetchFilteredMetar()
            filteredListMutableLiveData = MutableLiveData()

            CoroutineScope(Main).launch {
                filteredListMutableLiveData!!.value = filteredStationList
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

    fun getMutableFilteredList(): MutableLiveData<MutableList<String>>? = filteredListMutableLiveData

    override fun onDataFetchComplete(dataType: Int, dataBundle: Bundle) {
        when(dataType) {
            Constants.TYPE_FETCH_METAR_DATA_DETAILS -> {
                saveMetarData(dataBundle)
                notifyObservers(dataType, dataBundle)
            }
            Constants.TYPE_FETCH_FILTERED_STATION_LIST -> {
                // Save status to shared preference
                // Save list to local list and to database
                saveFilteredStationList(dataBundle)
            }
            Constants.TYPE_UPDATE_METAR_CACHE -> {
                // Save metar data
                saveMetarData(dataBundle)
            }
        }
    }

    private fun saveMetarData(dataBundle: Bundle) {
        val networkStatus = dataBundle.getInt(Constants.BUNDLE_KEY_STATUS)

        if (networkStatus == NETWORK_STATUS_DOWNLOAD_COMPLETED) {
            val metarData: MetarData? = dataBundle.getParcelable(Constants.BUNDLE_KEY_DATA)

            metarData?.let { metarData ->
                CoroutineScope(IO).launch {
                    val metar = repositoryManager.fetchMetar(metarData.code)

                    if (metar == null) {
                        repositoryManager.insertMetar(metarData)
                        Log.i(TAG, "saveMetarData: insertMetar metar data")
                    } else if (metar.decodedData.hashCode() != metarData.decodedData.hashCode()) {
                        repositoryManager.updateMetar(metarData)
                        Log.i(TAG, "saveMetarData: updateMetar metar data")
                    } else {
                        Log.i(TAG, "saveMetarData: No need to insert or update")
                    }
                }
                metarDataMap.put(metarData.code, metarData)
            }
        }
    }

    private fun saveFilteredStationList(dataBundle: Bundle) {
        val networkStatus = dataBundle.getInt(Constants.BUNDLE_KEY_STATUS)
        Log.i(TAG, "saveFilteredStationList: networkStatus = $networkStatus")

        when(networkStatus) {
            NETWORK_STATUS_DOWNLOAD_COMPLETED -> {
                // Save and update shared preference
                val stationList: ArrayList<String> = dataBundle.getStringArrayList(BUNDLE_KEY_DATA) as ArrayList<String>

                filteredStationList = mutableListOf()

                stationList.forEach { station ->
                    // Add to filtered list
                    filteredStationList!!.add(station)

                    // If not available in cached list
                    if (!metarDataMap.containsKey(station)) {
                        repositoryManager.insertMetar(MetarData(code = station))
                        metarDataMap[station] = MetarData(code = station)
                    }
                }
                CoroutineScope(Main).launch {
                    filteredListMutableLiveData?.value = filteredStationList
                }

                sharedPrefEditor.putString(SHARED_PREF_KEY_FILTERED_LIST_STATUS, STATUS_DOWNLOAD_COMPLETE)
                sharedPrefEditor.commit()
            }

            NETWORK_STATUS_DOWNLOAD_STARTED -> sharedPrefEditor.putString(SHARED_PREF_KEY_FILTERED_LIST_STATUS, STATUS_DOWNLOAD_STARTED)

            else -> sharedPrefEditor.putString(SHARED_PREF_KEY_FILTERED_LIST_STATUS, STATUS_NOT_AVAILABLE)
        }
    }
}
