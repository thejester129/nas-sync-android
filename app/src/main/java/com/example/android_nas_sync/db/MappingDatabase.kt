package com.example.android_nas_sync.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android_nas_sync.db.MappingDAO
import com.example.android_nas_sync.models.Mapping

@Database(entities = [Mapping::class], version = 1)
abstract class MappingDatabase : RoomDatabase() {
    abstract fun mappingDao(): MappingDAO
    companion object {

        private var INSTANCE: MappingDatabase? = null

        fun getInstance(context: Context): MappingDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, MappingDatabase::class.java,
                    "mappings.db")
                    .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
