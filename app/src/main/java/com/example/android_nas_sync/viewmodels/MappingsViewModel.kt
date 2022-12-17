package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.android_nas_sync.io.DeviceFileReader
import com.example.android_nas_sync.io.SmbShareConnector
import com.example.android_nas_sync.io.SmbFileWriter
import com.example.android_nas_sync.utils.TimeUtils
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.SyncingException
import com.example.android_nas_sync.repository.MappingsRepository
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.launch

class MappingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val db = Room.databaseBuilder(
        application.applicationContext,
        MappingDatabase::class.java, "mappings"
    ).build()

    private val repository:MappingsRepository = MappingsRepository(db, context)

    val mappings: LiveData<List<Mapping>> = repository.mappings
    val unseenSnackMessages = MutableLiveData<MutableList<String>>()
    val unseenNotifications = MutableLiveData<MutableList<String>>()
    var currentlyEditedMapping:MutableLiveData<Mapping> = MutableLiveData()
    var canDeleteCurrentlyEdited = false

    fun updateCurrentEdited() {
        viewModelScope.launch {
            val mapping = currentlyEditedMapping.value
            if (mapping != null) {
                repository.insert(mapping)
            }
        }
    }

    fun deleteCurrentlyEdited(){
        viewModelScope.launch {
            val mapping = currentlyEditedMapping.value
            if(mapping != null){
                repository.delete(mapping)
            }
        }
    }

    fun syncAllMappings(){
        viewModelScope.launch {
            mappings.value?.forEach{mapping ->
                repository.update(mapping.apply{
                    this.currentlySyncing = true
                })

                val result = repository.syncMapping(mapping)

                if(result.filedAdded > 0){
                    addSnackMessage("${result.filedAdded} files added")
                }
                if(result.filesFailedToAdd > 0){
                    addSnackMessage("${result.filesFailedToAdd} files failed to add")
                }
                if(result.filedAdded == 0 && result.filesFailedToAdd == 0 && result.errorMessage == null){
                   addSnackMessage("No new files found")
                }

                repository.update(mapping.apply{
                    this.currentlySyncing = false
                })
            }
        }
    }

    private fun addSnackMessage(message:String){
        val list = unseenSnackMessages.value ?: mutableListOf()
        list.add(message)
        unseenSnackMessages.postValue(list)
    }

    private fun addNotification(message:String){
        val list = unseenNotifications.value ?: mutableListOf()
        list.add(message)
        unseenNotifications.postValue(list)
    }

}
