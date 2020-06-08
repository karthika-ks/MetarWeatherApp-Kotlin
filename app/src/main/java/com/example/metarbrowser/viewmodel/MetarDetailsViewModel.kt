package com.example.metarbrowser.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.example.metarbrowser.model.interfaces.INetworkResponseCallback
import com.example.metarbrowser.model.managers.MetarBrowserManager
import com.example.metarbrowser.utilities.Constants
import com.example.metarbrowser.utilities.MetarData
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class MetarDetailsViewModel : ViewModel(), LifecycleObserver, INetworkResponseCallback, KoinComponent {
    val metarBrowserManager: MetarBrowserManager by inject()

    companion object {
        val TAG = MetarDetailsViewModel::class.java.simpleName
    }

    val isDataDownloadProgress : MutableLiveData<Boolean> = MutableLiveData()
    val hasNetworkConnectivity : MutableLiveData<Boolean> = MutableLiveData()
    val isStationAvailable : MutableLiveData<Boolean> = MutableLiveData()
    val isDataAvailable : MutableLiveData<Boolean> = MutableLiveData()
    val hasCachedDataAvailability : MutableLiveData<Boolean> = MutableLiveData()
    val stationName : MutableLiveData<String> = MutableLiveData()
    val stationCode : MutableLiveData<String> = MutableLiveData()
    val lastUpdatedTime : MutableLiveData<String> = MutableLiveData()
    val decodedData : MutableLiveData<String> = MutableLiveData()
    val rawData : MutableLiveData<String> = MutableLiveData()

    init {
        clearObservables()
        metarBrowserManager.registerCallback(this)
    }

    fun registerLifeCycleObserver(lifeCycle : Lifecycle) {
        lifeCycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        metarBrowserManager.unregisterCallback(this)
        clearObservables()
    }

    fun clearObservables() {
        isDataDownloadProgress.value = false
        hasNetworkConnectivity.value = true
        isStationAvailable.value = true
        isDataAvailable.value = false
        hasCachedDataAvailability.value = false
        stationName.value = ""
        stationCode.value = ""
        lastUpdatedTime.value = ""
        decodedData.value = ""
        rawData.value = ""
    }

    override fun onDataFetchComplete(dataType: Int, dataBundle: Bundle) {
        val metarData: MetarData? = dataBundle.getParcelable(Constants.BUNDLE_KEY_DATA)
        val networkStatus = dataBundle.getInt(Constants.BUNDLE_KEY_STATUS)
        Log.i(TAG, "onDataFetchComplete, networkStatus is $networkStatus")

        when(networkStatus) {
            Constants.NETWORK_STATUS_ERROR_NO_INTERNET -> {
                hasNetworkConnectivity.value = false
                isDataDownloadProgress.value = false
            }
            Constants.NETWORK_STATUS_ERROR_FILE_NOT_FOUND -> {
                isStationAvailable.value = false
                isDataDownloadProgress.value = false
            }
            Constants.NETWORK_STATUS_DOWNLOAD_STARTED -> isDataDownloadProgress.value = true
            Constants.NETWORK_STATUS_DOWNLOAD_COMPLETED -> {
                isDataDownloadProgress.value = false
                isDataAvailable.value = true
                stationCode.value = metarData?.code
                stationName.value = metarData?.stationName
                lastUpdatedTime.value = metarData?.lastUpdateTime
                decodedData.value = metarData?.decodedData
                rawData.value = metarData?.rawData
            }
        }
    }
}
