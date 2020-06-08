package com.example.metarbrowser.model.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Metar::class), version = 1, exportSchema = false)
abstract class MetarRoomDatabase: RoomDatabase() {

    abstract fun metarDao(): MetarDao

    companion object {
        @Volatile
        private var INSTANCE: MetarRoomDatabase? = null

        fun getDatabase(context: Context): MetarRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    MetarRoomDatabase::class.java,
                    "metar_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}