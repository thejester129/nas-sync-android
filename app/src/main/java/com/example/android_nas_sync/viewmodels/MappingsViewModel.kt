package com.example.android_nas_sync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.android_nas_sync.common.DeviceFileReader
import com.example.android_nas_sync.common.ShareConnector
import com.example.android_nas_sync.common.SmbFileWriter
import com.example.android_nas_sync.common.TimeUtils
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.models.SyncingException
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.launch

class MappingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val db = Room.databaseBuilder(
        application.applicationContext,
        MappingDatabase::class.java, "mappings"
    ).build()

    private val mappingDao = db.mappingDao()

    val mappings: LiveData<List<Mapping>> = mappingDao.getAll()
    val unseenSnackMessages = MutableLiveData<MutableList<String>>()
    val unseenNotifications = MutableLiveData<MutableList<String>>()
    var currentlyEditedMapping:MutableLiveData<Mapping> = MutableLiveData()
    var canDeleteCurrentlyEdited = false

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
            val mapping = currentlyEditedMapping.value
            if(mapping != null){
                mappingDao.delete(mapping)
            }
        }
    }

    fun syncAllMappings(){
        viewModelScope.launch {
            mappings.value?.forEach { mapping ->
                run{
                    try {
                        syncMapping(mapping)
                        mapping.error = null
                    } catch (e: Exception) {
                        if (e is SyncingException) {
                            mapping.error = e.message
                        } else {
                            mapping.error = "Failed to sync"
                        }
                        addSnackMessage("${mapping.destinationShare}/${mapping.destinationPath} failed to sync")
                    }
                    updateMapping(mapping)
                }
            }
        }
    }

    private suspend fun syncMapping(mapping: Mapping){
        val ipAddress = mapping.serverIp!!
        val shareName = mapping.destinationShare!!
        val sharePath = mapping.destinationPath
        val username = mapping.username
        val password = mapping.password
        val contentUri = mapping.sourceFolder!!

        var filesAdded = 0
        var share: DiskShare? = null
        val shareConnector = ShareConnector()
            share = shareConnector.connectToSmbShare(ipAddress, shareName, username, password)
        val fileWriter = SmbFileWriter(share)

        val phoneFiles = DeviceFileReader.readFilesAtContentUri(contentUri, context)

        val writeExceptions = mutableListOf<Exception>()//TODO do something with these?
        phoneFiles.forEach { file -> run{
            if(!fileWriter.fileExistsInShare(sharePath, file.name)){
                try{
                    fileWriter.writeFileToShare(file,sharePath )
                    filesAdded++
                }
                catch (e:Exception){
                    addSnackMessage("${file.name} failed to add")
                    writeExceptions.add(e)
                }
            }
        } }
        // TODO if deleted then synced again counts the same item twice
        // probs need to keep a list of items synced.. yuk
        mapping.filesSynced = mapping.filesSynced + filesAdded
        mapping.lastSynced = TimeUtils.unixTimestampNowSecs()
        updateMapping(mapping)

        addSnackMessage("$filesAdded files added")

        shareConnector.closeConnection()
    }

    private fun addSnackMessage(message:String){
        val list = unseenSnackMessages.value ?: mutableListOf<String>()
        list.add(message)
        unseenSnackMessages.postValue(list)
    }

    private fun addNotification(message:String){
        val list = unseenNotifications.value ?: mutableListOf<String>()
        list.add(message)
        unseenNotifications.postValue(list)
    }

    init{
    }
}
