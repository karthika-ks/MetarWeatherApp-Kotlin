package com.example.metarbrowser.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.metarbrowser.R
import com.example.metarbrowser.viewmodel.FilteredListViewModel
import kotlinx.android.synthetic.main.activity_search_from_list.view.*

data class Station(val stationCode: String)

class FilteredListFragment: Fragment() {
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = FilteredListViewModel()
        model.getFilteredList().observe(this, Observer { list ->
            Log.i(TAG, "Filtered list value changed, Size = ${list.size}")
            // Check shared preference value
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_station_list, container, true)
        return view
    }
}