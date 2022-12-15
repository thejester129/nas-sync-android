package com.example.android_nas_sync.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android_nas_sync.models.Mapping

@Dao
interface MappingDAO {
    @Query("SELECT * FROM mapping")
    fun getAll(): LiveData<List<Mapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: Mapping)

    @Delete
    suspend fun delete(mapping: Mapping)

    @Update
    suspend fun update(mapping: Mapping)
}
