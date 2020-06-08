package com.example.metarbrowser.model.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metar_table")
data class Metar (
    //@PrimaryKey(autoGenerate = true) val id: Int,
    @PrimaryKey@ColumnInfo(name = "station_code") val stationCode: String = "",
    @ColumnInfo(name = "station_name") val stationName: String = "",
    @ColumnInfo(name = "last_updated_time") val lastUpdatedTime: String = "",
    @ColumnInfo(name = "raw_data") val rawData: String = "",
    @ColumnInfo(name  = "decoded_data") val decodedData: String = "") {
}