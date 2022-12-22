package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.repository.MappingsRepository
import kotlinx.coroutines.launch

class MappingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val db = MappingDatabase.getInstance(context)

    private val repository:MappingsRepository = MappingsRepository(db, context)

    val mappings: LiveData<List<Mapping>> = repository.liveMappings
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
        // TODO this should probably just send a message to sync service instead
        viewModelScope.launch {
            mappings.value?.forEach{mapping ->
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
            }
        }
    }

    fun addSnackMessage(message:String){
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
