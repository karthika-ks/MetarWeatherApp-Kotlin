package com.example.metarbrowser.model.repository

import androidx.room.*

@Dao
interface MetarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetar(metar: Metar)

    @Update
    suspend fun updateMetar(metar: Metar)

    @Query("SELECT * FROM metar_table ORDER BY station_name ASC")
    fun fetchCachedMetar(): List<Metar>

    @Query("SELECT * FROM metar_table WHERE station_code LIKE 'ED%' ORDER BY station_name ASC")
    fun fetchFilteredMetar(): List<Metar>

    @Query("SELECT * FROM metar_table WHERE station_code = :stationCode")
    fun fetchMetar(stationCode: String): Metar?
}