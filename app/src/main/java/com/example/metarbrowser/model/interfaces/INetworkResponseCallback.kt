package com.example.metarbrowser.model.interfaces

import android.os.Bundle

interface INetworkResponseCallback {
    fun onDataFetchComplete(dataType: Int, dataBundle: Bundle)
}