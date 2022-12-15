package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    var currentlyEditedMapping:MutableLiveData<Mapping> = MutableLiveData()
    var canDeleteCurrentlyEdited = false

    fun addMapping(mapping: Mapping){
        viewModelScope.launch {
            mappingDao.insert(mapping)
        }
    }
    fun updateMapping(mapping: Mapping) {
        viewModelScope.launch {
            mappingDao.update(mapping)
        }
    }

    fun updateCurrentEdited() {
        viewModelScope.launch {
            val mapping = currentlyEditedMapping.value
            if (mapping != null) {
                mappingDao.insert(mapping)
            }
        }
    }

    fun deleteCurrentlyEdited(){
        viewModelScope.launch {
            val mapping = currentlyEditedMapping!!.value
            if(mapping != null){
                mappingDao.delete(mapping)
            }
        }
    }
    init {
    }
}
