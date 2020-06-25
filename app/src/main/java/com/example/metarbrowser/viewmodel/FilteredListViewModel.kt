package com.example.metarbrowser.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.metarbrowser.model.managers.MetarBrowserManager
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class FilteredListViewModel: ViewModel(), KoinComponent {
    private val metarBrowserManager: MetarBrowserManager by inject()
    private var filteredList: MutableLiveData<MutableList<String>>? = null

    fun getFilteredList(): MutableLiveData<MutableList<String>> {
        if (filteredList == null) {
            filteredList = MutableLiveData()
            filteredList = metarBrowserManager.getMutableFilteredList()
        }
        return filteredList as MutableLiveData<MutableList<String>>
    }
}