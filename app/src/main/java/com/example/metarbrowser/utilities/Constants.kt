package com.example.metarbrowser.utilities

object Constants {
    const val FILTER_STRING_GERMAN = "ED"
    const val METAR_LABEL_RAW_DATA = "ob:"

    const val TYPE_FETCH_METAR_DATA_DETAILS = 0
    const val TYPE_FETCH_FILTERED_STATION_LIST = 1
    const val TYPE_UPDATE_METAR_CACHE = 2

    const val NETWORK_STATUS_DOWNLOAD_STARTED: Int = 0
    const val NETWORK_STATUS_DOWNLOAD_COMPLETED = 1
    const val NETWORK_STATUS_ERROR_FILE_NOT_FOUND = 2
    const val NETWORK_STATUS_ERROR_NO_INTERNET = 3
    const val NETWORK_STATUS_DOWNLOADING_ERROR = 4

    const val BUNDLE_KEY_STATUS = "status"
    const val BUNDLE_KEY_DATA = "data"

    const val NETWORK_CONNECTED = 0
    const val NETWORK_DISCONNECTED = 1

    const val NETWORK_RESPONSE_WAITING_TIME = 250
    const val POST_NETWORK_SERVICE_TIME = 5
    const val KEEP_ALIVE_TIME: Long = 10
    const val DOWNLOAD_SCHEDULER_PERIOD: Long = 10

    const val SHARED_PREF_NAME = "MetarBrowser"
    const val SHARED_PREF_KEY_FILTERED_LIST_STATUS = "FILTERED_LIST_STATUS"

    const val STATUS_NOT_AVAILABLE = "NOT_AVAILABLE"
    const val STATUS_DOWNLOAD_STARTED = "DOWNLOAD_STARTED"
    const val STATUS_DOWNLOAD_COMPLETE = "DOWNLOAD_COMPLETE"
}