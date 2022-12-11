package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.android_nas_sync.common.MappingDatabase
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.ShareType
import kotlinx.coroutines.launch

class MappingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application.applicationContext,
        MappingDatabase::class.java, "mappings"
    ).build()

    private val mappingDao = db.mappingDao()

    val mappings: LiveData<List<Mapping>> = mappingDao.getAll()

    fun addMapping(mapping: Mapping){
        viewModelScope.launch {
            mappingDao.insert(mapping)
        }
    }
    init {
        if(mappingDao.getAll().value == null){
            addMapping(Mapping(1, ShareType.SMB, "phone/test", "192.168.0.45",
                "backup", "phone_pixel/camera", 123.toDouble()))
        }
    }
}
