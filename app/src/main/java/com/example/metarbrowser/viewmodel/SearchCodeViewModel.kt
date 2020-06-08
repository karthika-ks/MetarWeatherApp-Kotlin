package com.example.metarbrowser.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import com.example.metarbrowser.model.interfaces.INetworkResponseCallback
import com.example.metarbrowser.model.managers.MetarBrowserManager
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class SearchCodeViewModel : ViewModel(), LifecycleObserver, INetworkResponseCallback, KoinComponent {
    val metarBrowserManager: MetarBrowserManager by inject()

    companion object {
        val TAG = SearchCodeViewModel::class.java.simpleName
    }

    val editTextEntry : MutableLiveData<String> = MutableLiveData()
    val detailsViewModel : MetarDetailsViewModel = MetarDetailsViewModel()

    init {
        clearObservables()
        metarBrowserManager.registerCallback(this)
    }

    fun registerLifeCycleObserver(lifeCycle : Lifecycle) {
        lifeCycle.addObserver(this)
        detailsViewModel.registerLifeCycleObserver(lifeCycle)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearObservables() {
        metarBrowserManager.unregisterCallback(this)

        editTextEntry.value = ""
    }

    fun onCodeSearchClicked() {
        detailsViewModel.clearObservables()
        metarBrowserManager.fetchMetarData(editTextEntry.value.toString())
    }

    override fun onDataFetchComplete(dataType: Int, dataBundle: Bundle) {
        //TODO("Not yet implemented")
    }
}