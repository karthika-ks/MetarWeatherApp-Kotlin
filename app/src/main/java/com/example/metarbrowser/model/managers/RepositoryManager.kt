package com.example.metarbrowser.model.managers

import android.content.Context
import android.util.Log
import com.example.metarbrowser.model.repository.Metar
import com.example.metarbrowser.model.repository.MetarRoomDatabase
import com.example.metarbrowser.utilities.MetarData
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class RepositoryManager: KoinComponent {
    private val TAG: String = javaClass.simpleName
    private val context: Context by inject()

    fun insertMetar(metarData: MetarData) {

        CoroutineScope(Dispatchers.Default).launch {

            val metar: Metar? = metarData.toMetar()
            val metarDao = MetarRoomDatabase.getDatabase(context).metarDao()

            if (metar != null) {
                Log.i(TAG, "Needs to insert ${metar?.stationCode} to DB")
                metarDao.insertMetar(metar)
            }
        }
    }

    suspend fun fetchCachedMetar() : MutableList<MetarData> {

        val metarDataList: MutableList<MetarData> = mutableListOf()

        val job = CoroutineScope(Dispatchers.Default).launch {

            val metarDao =
                MetarRoomDatabase.getDatabase(context).metarDao()

            val metarDatas: List<Metar> = metarDao.fetchCachedMetar()

            if (!metarDatas.isEmpty()) {
                for (metar in metarDatas) {
                    metarDataList.add(metar.toMetarData())
                }
            }
        }

        job.join()

        Log.i(TAG, "Cached data size = ${metarDataList.size}")
        return metarDataList
    }

    suspend fun fetchFilteredMetar() : List<MetarData> {

        val metarDataList: MutableList<MetarData> = mutableListOf()

        val job = CoroutineScope(Dispatchers.Default).launch {
            val metarDao = MetarRoomDatabase.getDatabase(context).metarDao()
            val metarDatas: List<Metar> = metarDao.fetchFilteredMetar()

            if (!metarDatas.isEmpty()) {
                for (metar in metarDatas) {
                    Log.i(TAG, "Code: ${metar.stationCode}, Station Name: ${metar.stationCode}, Decoded Data: ${metar.decodedData}")
                    metarDataList.add(metar.toMetarData())
                }
            }
        }

        job.join()

        return metarDataList
    }

    fun MetarData.toMetar() = Metar(
        stationCode = "$code",
        stationName = "$stationName",
        lastUpdatedTime = "$lastUpdateTime",
        rawData = "$rawData",
        decodedData = "$decodedData"
    )

    fun Metar.toMetarData() = MetarData(
        code = "$stationCode",
        stationName = "$stationName",
        lastUpdateTime = "$lastUpdatedTime",
        rawData = "$rawData",
        decodedData = "$decodedData"
    )
}