package com.example.metarbrowser.utilities

object Constants {
    const val FILTER_STRING_GERMAN = "ED"
    const val METAR_LABEL_RAW_DATA = "ob:"

    const val TYPE_FETCH_METAR_DATA_DETAILS = 0

    const val NETWORK_STATUS_DOWNLOAD_STARTED: Int = 0
    const val NETWORK_STATUS_DOWNLOAD_COMPLETED = 1
    const val NETWORK_STATUS_ERROR_FILE_NOT_FOUND = 2
    const val NETWORK_STATUS_ERROR_NO_INTERNET = 3

    const val BUNDLE_KEY_STATUS = "status"
    const val BUNDLE_KEY_DATA = "data"

    const val NETWORK_CONNECTED = 0
    const val NETWORK_DISCONNECTED = 1
}