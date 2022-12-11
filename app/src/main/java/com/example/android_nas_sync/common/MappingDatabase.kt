package com.example.android_nas_sync.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.android_nas_sync.db.MappingDAO
import com.example.android_nas_sync.models.Mapping

@Database(entities = [Mapping::class], version = 1)
abstract class MappingDatabase : RoomDatabase() {
    abstract fun mappingDao(): MappingDAO
}
