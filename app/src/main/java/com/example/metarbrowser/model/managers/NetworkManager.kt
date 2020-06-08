package com.example.metarbrowser.model.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.metarbrowser.model.interfaces.INetworkChangeListener
import com.example.metarbrowser.utilities.Constants.NETWORK_CONNECTED
import com.example.metarbrowser.utilities.Constants.NETWORK_DISCONNECTED
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

object NetworkManager: KoinComponent {

    private var responseCallbacks : MutableList<INetworkChangeListener> = mutableListOf()
    private val context: Context by inject()

    fun registerNetworkChangeReceiver() {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();

        val networkCallback: NetworkCallback = object : NetworkCallback() {
            override fun onAvailable(net: Network) {
                notifyNetworkChange(NETWORK_CONNECTED)
            }

            override fun onLost(net: Network) {
                notifyNetworkChange(NETWORK_DISCONNECTED)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun registerChangeListener(networkListener: INetworkChangeListener) {
        responseCallbacks.add(networkListener)
    }

    fun notifyNetworkChange(networkStatus: Int) {
        for (callback in responseCallbacks) {
            when(networkStatus) {
                NETWORK_CONNECTED -> callback.onConnected()
                NETWORK_DISCONNECTED -> callback.onDisconnected()
            }
        }
    }
}