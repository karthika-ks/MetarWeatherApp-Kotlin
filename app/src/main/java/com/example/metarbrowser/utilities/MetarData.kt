package com.example.metarbrowser.utilities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MetarData (var code : String = "", var stationName : String = ""
                      , var lastUpdateTime : String = "", var rawData : String = ""
                      , var decodedData : String = "") : Parcelable