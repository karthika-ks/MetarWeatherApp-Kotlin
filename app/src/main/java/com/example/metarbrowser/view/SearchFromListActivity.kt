package com.example.metarbrowser.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.metarbrowser.R
import com.example.metarbrowser.viewmodel.FilteredListViewModel

class SearchFromListActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = FilteredListViewModel()
        model.getFilteredList().observe(this, Observer { list ->
            Log.i(TAG, "Filtered list value changed, Size = ${list.size}")
            // Check shared preference value
        })
    }
}
